<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Coupon extends Model
{
    protected $fillable = [
        'code',
        'title_ar',
        'title_en',
        'discount_type',
        'discount_value',
        'max_discount',
        'min_order_total',
        'usage_limit',
        'usage_limit_per_user',
        'used_count',
        'starts_at',
        'expires_at',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'discount_value' => 'decimal:3',
            'max_discount' => 'decimal:3',
            'min_order_total' => 'decimal:3',
            'starts_at' => 'datetime',
            'expires_at' => 'datetime',
            'is_active' => 'boolean',
        ];
    }

    // ─── Relationships ───────────────────────────────────────

    public function usages()
    {
        return $this->hasMany(CouponUsage::class);
    }

    // ─── Scopes ──────────────────────────────────────────────

    public function scopeActive($query)
    {
        return $query->where('is_active', true)
            ->where(fn ($q) => $q->whereNull('starts_at')->orWhere('starts_at', '<=', now()))
            ->where(fn ($q) => $q->whereNull('expires_at')->orWhere('expires_at', '>=', now()));
    }

    // ─── Accessors ───────────────────────────────────────────

    public function getTitleAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->title_ar : $this->title_en;
    }

    // ─── Validation ──────────────────────────────────────────

    /**
     * Why this coupon cannot be used, or null when it is usable.
     */
    public function rejectionReason(?int $userId, float $orderTotal): ?string
    {
        if (!$this->is_active) {
            return __('coupons.inactive');
        }

        if ($this->starts_at && $this->starts_at->isFuture()) {
            return __('coupons.not_started');
        }

        if ($this->expires_at && $this->expires_at->isPast()) {
            return __('coupons.expired');
        }

        if ($this->min_order_total > 0 && $orderTotal < (float) $this->min_order_total) {
            return __('coupons.min_order', ['amount' => number_format((float) $this->min_order_total, 3)]);
        }

        if ($this->usage_limit !== null && $this->used_count >= $this->usage_limit) {
            return __('coupons.limit_reached');
        }

        if ($userId !== null && $this->usage_limit_per_user !== null) {
            $userUsage = $this->usages()->where('user_id', $userId)->count();
            if ($userUsage >= $this->usage_limit_per_user) {
                return __('coupons.user_limit_reached');
            }
        }

        return null;
    }

    /**
     * Discount in JOD for the given order subtotal, capped so it never exceeds it.
     */
    public function discountFor(float $orderTotal): float
    {
        $discount = $this->discount_type === 'percent'
            ? $orderTotal * ((float) $this->discount_value / 100)
            : (float) $this->discount_value;

        if ($this->max_discount !== null) {
            $discount = min($discount, (float) $this->max_discount);
        }

        return round(min($discount, $orderTotal), 3);
    }
}
