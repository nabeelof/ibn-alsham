<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Order;
use App\Services\DeliveryService;
use App\Services\NotificationService;
use App\Services\OrderService;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class OrderController extends Controller
{
    use ApiResponse;

    public function __construct(
        private OrderService $orderService,
        private DeliveryService $deliveryService,
        private NotificationService $notificationService,
    ) {}

    /**
     * Create a new order.
     */
    public function store(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'type' => 'required|in:pickup,delivery',
            'items' => 'required|array|min:1',
            'items.*.product_id' => 'required|exists:products,id',
            'items.*.quantity' => 'required|integer|min:1',
            'items.*.option_ids' => 'nullable|array',
            'items.*.option_ids.*' => 'exists:product_options,id',
            'notes' => 'nullable|string|max:1000',
            // Delivery required fields
            'delivery' => 'required_if:type,delivery|array',
            'delivery.address' => 'required_if:type,delivery|string',
            'delivery.lat' => 'required_if:type,delivery|numeric',
            'delivery.lng' => 'required_if:type,delivery|numeric',
        ]);

        if ($validator->fails()) {
            return $this->error(__('validations.validation_error'), 422, $validator->errors());
        }

        try {
            $order = $this->orderService->createOrder(
                $request->user()->id,
                $request->type,
                $request->items,
                $request->delivery ?? [],
                $request->notes
            );

            return $this->success(
                $this->formatOrderDetails($order, app()->getLocale()),
                __('orders.created_successfully')
            );
        } catch (\Exception $e) {
            return $this->error(__('orders.creation_failed'), 500, ['error' => $e->getMessage()]);
        }
    }

    /**
     * Get user's orders (current or previous).
     */
    public function index(Request $request): JsonResponse
    {
        $locale = app()->getLocale();
        $query = $request->user()->orders()->orderByDesc('created_at');

        if ($request->input('tab') === 'previous') {
            $query->previous();
        } else {
            $query->current();
        }

        $orders = $query->get()->map(fn ($o) => [
            'id' => $o->id,
            'order_number' => $o->order_number,
            'type' => $o->type,
            'status' => $o->status,
            'status_label' => $locale === 'ar' ? $o->status_label_ar : $o->status_label_en,
            'total' => $o->total,
            'item_count' => $o->items()->sum('quantity'),
            'created_at' => $o->created_at->format('Y-m-d H:i'),
            'can_cancel' => $o->can_cancel,
            'cancellation_deadline' => $o->cancellation_deadline?->format('Y-m-d H:i:s'),
            'estimated_time' => $o->estimated_time,
            'confirmed_at' => $o->confirmed_at?->format('Y-m-d H:i'),
            'notes' => $o->notes,
            'needs_delivery_confirmation' => $o->needs_delivery_confirmation,
            'has_review' => $o->has_review,
            'rating' => $o->rating,
        ]);

        return $this->success($orders);
    }

    /**
     * Get order details.
     */
    public function show(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()
            ->with(['items.product', 'items.options.productOption'])
            ->findOrFail($id);

        return $this->success($this->formatOrderDetails($order, app()->getLocale()));
    }

    /**
     * Poll order status (lightweight endpoint for tracking).
     */
    public function status(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()->findOrFail($id);
        $locale = app()->getLocale();

        return $this->success([
            'id' => $order->id,
            'status' => $order->status,
            'status_label' => $locale === 'ar' ? $order->status_label_ar : $order->status_label_en,
            'can_cancel' => $order->can_cancel,
            'cancellation_deadline' => $order->cancellation_deadline?->format('Y-m-d H:i:s'),
            'estimated_time' => $order->estimated_time,
            'confirmed_at' => $order->confirmed_at?->format('Y-m-d H:i'),
            'needs_delivery_confirmation' => $order->needs_delivery_confirmation,
            'has_review' => $order->has_review,
        ]);
    }

    /**
     * Cancel an order.
     */
    public function cancel(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()->findOrFail($id);

        if (!$order->can_cancel) {
            return $this->error(__('orders.cannot_cancel'), 400);
        }

        $order->cancel($request->input('reason'));

        return $this->success(null, __('orders.cancelled_successfully'));
    }

    /**
     * User confirms delivery was received.
     */
    public function confirmDelivery(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()->findOrFail($id);

        if ($order->delivery_confirmed_at) {
            return $this->error('Delivery already confirmed', 400);
        }

        if (in_array($order->status, ['cancelled', 'completed'])) {
            return $this->error('Order is not in a confirmable state', 400);
        }

        $order->confirmDelivery();

        return $this->success(null, 'Delivery confirmed successfully');
    }

    /**
     * Submit review for an order.
     */
    public function review(int $id, Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'rating' => 'required|integer|min:1|max:5',
            'review_note' => 'nullable|string|max:500',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $order = $request->user()->orders()->findOrFail($id);

        if ($order->has_review) {
            return $this->error('Review already submitted', 400);
        }

        if (!in_array($order->status, ['delivered', 'completed'])) {
            // Allow review even if not yet marked delivered (user confirms via this)
            if (!in_array($order->status, ['confirmed', 'preparing', 'ready', 'out_for_delivery'])) {
                return $this->error('Order cannot be reviewed in this state', 400);
            }
            $order->confirmDelivery();
        }

        $order->submitReview(
            $request->input('rating'),
            $request->input('review_note')
        );

        return $this->success(null, 'Review submitted successfully');
    }

    public function reorder(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()->findOrFail($id);

        return $this->success([
            'result' => true,
            'message' => 'Reorder created',
            'order_id' => $order->id,
        ]);
    }

    public function purchaseHistory(Request $request): JsonResponse
    {
        $orders = $request->user()->orders()->orderByDesc('created_at')->paginate(20);
        $locale = app()->getLocale();

        $data = $orders->map(fn ($o) => [
            'id' => $o->id,
            'code' => $o->order_number,
            'date' => $o->created_at->format('Y-m-d'),
            'payment_status' => $o->payment_method,
            'delivery_status' => $o->status,
            'grand_total' => $o->total,
            'links' => [
                'details' => null,
            ],
        ]);

        return response()->json([
            'data' => $data,
            'success' => true,
            'status' => 200,
            'meta' => [
                'current_page' => $orders->currentPage(),
                'last_page' => $orders->lastPage(),
                'total' => $orders->total(),
            ],
        ]);
    }

    public function purchaseHistoryDetails(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()->findOrFail($id);
        return response()->json([
            'data' => $this->formatOrderDetails($order->loadMissing(['items.options']), app()->getLocale()),
            'success' => true,
            'status' => 200,
        ]);
    }

    public function purchaseHistoryItems(int $id, Request $request): JsonResponse
    {
        $order = $request->user()->orders()->with(['items.options'])->findOrFail($id);

        $items = $order->items->map(fn ($item) => [
            'id' => $item->id,
            'product_id' => $item->product_id,
            'name' => app()->getLocale() === 'ar' ? $item->product_name_ar : $item->product_name_en,
            'quantity' => $item->quantity,
            'price' => $item->unit_price,
            'total' => $item->total,
        ]);

        return response()->json([
            'data' => $items,
            'success' => true,
            'status' => 200,
        ]);
    }

    public function digitalPurchasedList(Request $request): JsonResponse
    {
        return response()->json([
            'data' => [],
            'success' => true,
            'status' => 200,
        ]);
    }

    private function formatOrderDetails(Order $order, string $locale): array
    {
        return [
            'id' => $order->id,
            'order_number' => $order->order_number,
            'type' => $order->type,
            'status' => $order->status,
            'status_label' => $locale === 'ar' ? $order->status_label_ar : $order->status_label_en,
            'subtotal' => $order->subtotal,
            'delivery_fee' => $order->delivery_fee,
            'total' => $order->total,
            'address' => $order->address,
            'notes' => $order->notes,
            'payment_method' => $order->payment_method,
            'created_at' => $order->created_at->format('Y-m-d H:i'),
            'confirmed_at' => $order->confirmed_at?->format('Y-m-d H:i'),
            'estimated_time' => $order->estimated_time,
            'cancellation_deadline' => $order->cancellation_deadline?->format('Y-m-d H:i:s'),
            'can_cancel' => $order->can_cancel,
            'needs_delivery_confirmation' => $order->needs_delivery_confirmation,
            'has_review' => $order->has_review,
            'rating' => $order->rating,
            'review_note' => $order->review_note,
            'items' => $order->items->map(fn ($item) => [
                'id' => $item->id,
                'product_id' => $item->product_id,
                'name' => $locale === 'ar' ? $item->product_name_ar : $item->product_name_en,
                'quantity' => $item->quantity,
                'unit_price' => $item->unit_price,
                'discount_price' => $item->discount_price,
                'options_total' => $item->options_total,
                'total' => $item->total,
                'image' => $item->product?->image_url,
                'options' => $item->options->map(fn ($opt) => [
                    'id' => $opt->id,
                    'name' => $locale === 'ar' ? $opt->option_name_ar : $opt->option_name_en,
                    'price' => $opt->price,
                ]),
            ]),
        ];
    }
}
