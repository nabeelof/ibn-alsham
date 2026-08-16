<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Order;
use App\Models\PaymentMethod;
use App\Services\OrderService;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class PaymentController extends Controller
{
    use ApiResponse;

    public function __construct(
        private OrderService $orderService,
    ) {}

    /**
     * Payment methods available in Jordan, filtered by order type.
     */
    public function paymentTypes(Request $request): JsonResponse
    {
        $methods = PaymentMethod::active()
            ->forOrderType($request->input('type'))
            ->orderBy('sort_order')
            ->get()
            ->map(fn (PaymentMethod $m) => [
                'key' => $m->key,
                'payment_type' => $m->key,
                'payment_type_key' => $m->key,
                'name' => $m->name,
                'title' => $m->name,
                'description' => $m->description,
                'image' => $m->icon_url,
                'account_alias' => $m->account_alias,
                'requires_reference' => $m->requires_reference,
                'is_online' => $m->is_online,
            ]);

        return $this->success($methods);
    }

    public function orderStore(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'type' => 'required|in:pickup,delivery',
            'items' => 'required|array|min:1',
            'items.*.product_id' => 'required|exists:products,id',
            'items.*.quantity' => 'required|integer|min:1',
            'items.*.option_ids' => 'nullable|array',
            'items.*.option_ids.*' => 'exists:product_options,id',
            'notes' => 'nullable|string|max:1000',
            'coupon_code' => 'nullable|string|max:64',
            'payment_method' => 'nullable|string|exists:payment_methods,key',
            'payment_reference' => 'nullable|string|max:120',
            'delivery' => 'nullable|required_if:type,delivery|array',
            'delivery.address' => 'nullable|required_if:type,delivery|string',
            'delivery.lat' => 'nullable|required_if:type,delivery|numeric',
            'delivery.lng' => 'nullable|required_if:type,delivery|numeric',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'combined_order_id' => null,
                'result' => false,
                'message' => __('validations.validation_error'),
                'errors' => $validator->errors(),
            ], 422);
        }

        $userId = auth('sanctum')->id() ?? $request->input('user_id');
        if (!$userId) {
            return response()->json([
                'combined_order_id' => null,
                'result' => false,
                'message' => 'Missing user_id',
            ], 422);
        }

        $method = $request->input('payment_method', 'cash');
        $paymentMethod = PaymentMethod::active()->where('key', $method)->first();
        if (!$paymentMethod) {
            return response()->json([
                'combined_order_id' => null,
                'result' => false,
                'message' => __('payments.method_unavailable'),
            ], 422);
        }

        if ($paymentMethod->requires_reference && !$request->filled('payment_reference')) {
            return response()->json([
                'combined_order_id' => null,
                'result' => false,
                'message' => __('payments.reference_required'),
            ], 422);
        }

        $order = $this->orderService->createOrder(
            $userId,
            $request->input('type'),
            $request->input('items'),
            $request->input('delivery', []),
            $request->input('notes'),
            $request->input('coupon_code'),
            $paymentMethod->key,
            $request->input('payment_reference'),
        );

        return response()->json([
            'combined_order_id' => $order->id,
            'order_id' => $order->id,
            'order_number' => $order->order_number,
            'subtotal' => (float) $order->subtotal,
            'discount_amount' => (float) $order->discount_amount,
            'delivery_fee' => (float) $order->delivery_fee,
            'total' => (float) $order->total,
            'payment_method' => $order->payment_method,
            'payment_status' => $order->payment_status,
            'result' => true,
            'message' => 'Order created successfully',
        ]);
    }

    public function payCod(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'order_id' => 'required|exists:orders,id',
        ]);

        if ($validator->fails()) {
            return $this->error(__('validations.validation_error'), 422, $validator->errors());
        }

        $order = Order::findOrFail($request->input('order_id'));
        $user = auth('sanctum')->user();
        if ($user && $order->user_id !== $user->id) {
            return $this->error('Forbidden', 403);
        }

        $order->update([
            'payment_method' => 'cash',
            'payment_status' => 'unpaid',
        ]);

        return $this->success(['result' => true], message: __('payments.recorded'));
    }

    /**
     * Record a CliQ transfer against an order. The restaurant confirms it manually.
     */
    public function payCliq(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'order_id' => 'required|exists:orders,id',
            'payment_reference' => 'required|string|max:120',
        ]);

        if ($validator->fails()) {
            return $this->error(__('validations.validation_error'), 422, $validator->errors());
        }

        if (!PaymentMethod::active()->where('key', 'cliq')->exists()) {
            return $this->error(__('payments.method_unavailable'), 422);
        }

        $order = Order::findOrFail($request->input('order_id'));
        $user = auth('sanctum')->user();
        if ($user && $order->user_id !== $user->id) {
            return $this->error('Forbidden', 403);
        }

        $order->update([
            'payment_method' => 'cliq',
            'payment_status' => 'pending',
            'payment_reference' => $request->input('payment_reference'),
        ]);

        return $this->success(['result' => true], message: __('payments.pending_review'));
    }

    public function payWallet(): JsonResponse
    {
        return $this->error(__('payments.method_unavailable'), 422);
    }

    public function payManual(): JsonResponse
    {
        return $this->error(__('payments.method_unavailable'), 422);
    }

    public function paypalUrl(): JsonResponse
    {
        // Not offered in the Jordan rollout — CliQ and cash cover the market.
        return $this->error(__('payments.method_unavailable'), 501);
    }
}
