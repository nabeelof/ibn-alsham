<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Order;
use App\Services\OrderService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class PaymentController extends Controller
{
    public function __construct(
        private OrderService $orderService,
    ) {}

    public function paymentTypes(): JsonResponse
    {
        return response()->json([
            [
                'payment_type' => 'cod',
                'payment_type_key' => 'cod',
                'image' => null,
                'name' => 'Cash on Delivery',
                'title' => 'Cash on Delivery',
                'offline_payment_id' => null,
                'details' => null,
            ],
            [
                'payment_type' => 'paypal',
                'payment_type_key' => 'paypal',
                'image' => null,
                'name' => 'PayPal',
                'title' => 'PayPal',
                'offline_payment_id' => null,
                'details' => null,
            ],
            [
                'payment_type' => 'card',
                'payment_type_key' => 'card',
                'image' => null,
                'name' => 'Card',
                'title' => 'Credit/Debit Card',
                'offline_payment_id' => null,
                'details' => null,
            ],
        ]);
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
            'delivery' => 'required_if:type,delivery|array',
            'delivery.address' => 'required_if:type,delivery|string',
            'delivery.lat' => 'required_if:type,delivery|numeric',
            'delivery.lng' => 'required_if:type,delivery|numeric',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'combined_order_id' => null,
                'result' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors(),
            ], 422);
        }

        $userId = $request->user()?->id ?? $request->input('user_id');
        if (!$userId) {
            return response()->json([
                'combined_order_id' => null,
                'result' => false,
                'message' => 'Missing user_id',
            ], 422);
        }

        $order = $this->orderService->createOrder(
            $userId,
            $request->input('type'),
            $request->input('items'),
            $request->input('delivery', []),
            $request->input('notes')
        );

        return response()->json([
            'combined_order_id' => $order->id,
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
            return response()->json([
                'result' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors(),
            ], 422);
        }

        $order = Order::findOrFail($request->input('order_id'));
        $user = $request->user();
        if ($user && $order->user_id !== $user->id) {
            return response()->json([
                'result' => false,
                'message' => 'Forbidden',
            ], 403);
        }

        $order->update([
            'payment_method' => 'cod',
        ]);

        return response()->json([
            'result' => true,
            'message' => 'Payment marked as COD',
        ]);
    }

    public function payWallet(): JsonResponse
    {
        return response()->json([
            'result' => false,
            'message' => 'Wallet payments not configured',
        ]);
    }

    public function payManual(): JsonResponse
    {
        return response()->json([
            'result' => false,
            'message' => 'Manual payments not configured',
        ]);
    }

    public function paypalUrl(Request $request): JsonResponse
    {
        return response()->json([
            'result' => false,
            'url' => null,
            'message' => 'PayPal is not configured yet',
        ], 501);
    }
}
