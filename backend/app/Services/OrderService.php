<?php

namespace App\Services;

use App\Jobs\AutoConfirmOrderJob;
use App\Jobs\CheckDeliveryStatusJob;
use App\Models\Order;
use App\Models\OrderItem;
use App\Models\OrderItemOption;
use App\Models\Product;
use App\Models\ProductOption;
use Illuminate\Support\Facades\DB;

class OrderService
{
    public function __construct(
        private DeliveryService $deliveryService,
        private CouponService $couponService,
    ) {}

    /**
     * Create a new order from cart items.
     *
     * @param int $userId
     * @param string $type 'pickup' or 'delivery'
     * @param array $items [{product_id, quantity, option_ids: []}]
     * @param array $deliveryData {address, lat, lng} (for delivery only)
     * @param string|null $notes
     * @param string|null $couponCode
     * @param string $paymentMethod one of the payment_methods keys
     * @param string|null $paymentReference CliQ / transfer reference
     * @return Order
     */
    public function createOrder(
        int $userId,
        string $type,
        array $items,
        array $deliveryData = [],
        ?string $notes = null,
        ?string $couponCode = null,
        string $paymentMethod = 'cash',
        ?string $paymentReference = null,
    ): Order {
        return DB::transaction(function () use (
            $userId,
            $type,
            $items,
            $deliveryData,
            $notes,
            $couponCode,
            $paymentMethod,
            $paymentReference,
        ) {
            $subtotal = 0;
            $deliveryFee = 0;
            $distanceKm = null;

            // Calculate delivery fee
            if ($type === 'delivery' && !empty($deliveryData['lat']) && !empty($deliveryData['lng'])) {
                // Restaurant coordinates (configure in settings)
                $restaurantLat = 31.9539; // Amman default
                $restaurantLng = 35.9106;

                $distanceKm = $this->deliveryService->calculateDistance(
                    $restaurantLat, $restaurantLng,
                    (float) $deliveryData['lat'],
                    (float) $deliveryData['lng']
                );

                $deliveryFee = $this->deliveryService->calculateDeliveryFee($distanceKm);
            }

            // Create order
            $order = Order::create([
                'user_id' => $userId,
                'order_number' => Order::generateOrderNumber(),
                'type' => $type,
                'status' => 'pending',
                'subtotal' => 0, // will update below
                'delivery_fee' => $deliveryFee,
                'total' => 0, // will update below
                'address' => $deliveryData['address'] ?? null,
                'lat' => $deliveryData['lat'] ?? null,
                'lng' => $deliveryData['lng'] ?? null,
                'distance_km' => $distanceKm,
                'notes' => $notes,
                'payment_method' => $paymentMethod,
                'payment_status' => $paymentReference !== null ? 'pending' : 'unpaid',
                'payment_reference' => $paymentReference,
            ]);

            // Create order items
            foreach ($items as $itemData) {
                $product = Product::findOrFail($itemData['product_id']);
                $quantity = (int) $itemData['quantity'];
                $unitPrice = (float) $product->price;
                $discountPrice = $product->discounted_price ? (float) $product->discounted_price : null;
                $effectivePrice = $discountPrice ?? $unitPrice;

                // Calculate options total
                $optionsTotal = 0;
                $selectedOptions = [];

                if (!empty($itemData['option_ids'])) {
                    $options = ProductOption::whereIn('id', $itemData['option_ids'])
                        ->where('product_id', $product->id)
                        ->get();

                    foreach ($options as $option) {
                        $optionsTotal += (float) $option->price;
                        $selectedOptions[] = $option;
                    }
                }

                $itemTotal = ($effectivePrice * $quantity) + ($optionsTotal * $quantity);
                $subtotal += $itemTotal;

                $orderItem = OrderItem::create([
                    'order_id' => $order->id,
                    'product_id' => $product->id,
                    'product_name_ar' => $product->name_ar,
                    'product_name_en' => $product->name_en,
                    'quantity' => $quantity,
                    'unit_price' => $unitPrice,
                    // Snapshot the cost so historical margins survive price changes.
                    'unit_cost' => $product->cost_price,
                    'discount_price' => $discountPrice,
                    'options_total' => $optionsTotal * $quantity,
                    'total' => $itemTotal,
                ]);

                // Create order item options
                foreach ($selectedOptions as $option) {
                    OrderItemOption::create([
                        'order_item_id' => $orderItem->id,
                        'product_option_id' => $option->id,
                        'option_name_ar' => $option->name_ar,
                        'option_name_en' => $option->name_en,
                        'price' => $option->price,
                    ]);
                }

                // Increment product order count
                $product->incrementOrderCount($quantity);
            }

            // Apply the coupon against the item subtotal (delivery is never discounted)
            $discount = 0.0;
            $coupon = null;
            if ($couponCode) {
                $result = $this->couponService->validate($couponCode, $userId, $subtotal);
                if ($result['ok']) {
                    $coupon = $result['coupon'];
                    $discount = $result['discount'];
                }
            }

            // Update order totals
            $order->update([
                'subtotal' => $subtotal,
                'coupon_id' => $coupon?->id,
                'coupon_code' => $coupon?->code,
                'discount_amount' => $discount,
                'total' => round($subtotal - $discount + $deliveryFee, 3),
            ]);

            if ($coupon) {
                $this->couponService->redeem($coupon, $order, $discount);
            }

            // Schedule auto-confirmation after 1 minute (testing — later configurable)
            AutoConfirmOrderJob::dispatch($order->id)->delay(now()->addMinute());

            return $order->fresh(['items.options']);
        });
    }
}
