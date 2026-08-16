<?php

namespace App\Services;

use App\Models\Coupon;
use App\Models\CouponUsage;
use App\Models\Order;

class CouponService
{
    /**
     * Validate a coupon code against an order subtotal.
     *
     * @return array{ok: bool, coupon: ?Coupon, discount: float, message: ?string}
     */
    public function validate(string $code, ?int $userId, float $subtotal): array
    {
        $coupon = Coupon::where('code', strtoupper(trim($code)))->first();

        if (!$coupon) {
            return ['ok' => false, 'coupon' => null, 'discount' => 0.0, 'message' => __('coupons.not_found')];
        }

        $reason = $coupon->rejectionReason($userId, $subtotal);
        if ($reason !== null) {
            return ['ok' => false, 'coupon' => $coupon, 'discount' => 0.0, 'message' => $reason];
        }

        return [
            'ok' => true,
            'coupon' => $coupon,
            'discount' => $coupon->discountFor($subtotal),
            'message' => null,
        ];
    }

    /**
     * Record that a coupon was consumed by an order.
     */
    public function redeem(Coupon $coupon, Order $order, float $discount): void
    {
        CouponUsage::create([
            'coupon_id' => $coupon->id,
            'user_id' => $order->user_id,
            'order_id' => $order->id,
            'discount_amount' => $discount,
        ]);

        $coupon->increment('used_count');
    }

    /**
     * Shape a coupon for the API.
     */
    public function present(Coupon $coupon, float $discount = 0.0): array
    {
        return [
            'id' => $coupon->id,
            'code' => $coupon->code,
            'title' => $coupon->title,
            'discount_type' => $coupon->discount_type,
            'discount_value' => (float) $coupon->discount_value,
            'max_discount' => $coupon->max_discount !== null ? (float) $coupon->max_discount : null,
            'min_order_total' => (float) $coupon->min_order_total,
            'discount_amount' => round($discount, 3),
            'expires_at' => $coupon->expires_at?->toIso8601String(),
        ];
    }
}
