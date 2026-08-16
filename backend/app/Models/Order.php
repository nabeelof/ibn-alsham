<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Order extends Model
{
    protected $fillable = [
        'user_id',
        'order_number',
        'type',
        'status',
        'subtotal',
        'delivery_fee',
        'total',
        'address',
        'lat',
        'lng',
        'distance_km',
        'notes',
        'coupon_id',
        'coupon_code',
        'discount_amount',
        'payment_method',
        'payment_status',
        'payment_reference',
        'confirmed_at',
        'estimated_time',
        'cancellation_deadline',
        'cancel_reason',
        'rating',
        'review_note',
        'delivery_confirmed_at',
        'auto_confirmed',
    ];

    protected function casts(): array
    {
        return [
            'subtotal' => 'decimal:3',
            'delivery_fee' => 'decimal:3',
            'discount_amount' => 'decimal:3',
            'total' => 'decimal:3',
            'lat' => 'decimal:7',
            'lng' => 'decimal:7',
            'distance_km' => 'decimal:2',
            'confirmed_at' => 'datetime',
            'cancellation_deadline' => 'datetime',
            'delivery_confirmed_at' => 'datetime',
            'auto_confirmed' => 'boolean',
        ];
    }

    // ─── Relationships ───────────────────────────────────────

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function items()
    {
        return $this->hasMany(OrderItem::class);
    }

    public function coupon()
    {
        return $this->belongsTo(Coupon::class);
    }

    // ─── Scopes ──────────────────────────────────────────────

    public function scopeCurrent($query)
    {
        return $query->whereIn('status', [
            'pending', 'confirmed', 'preparing', 'ready', 'out_for_delivery',
        ]);
    }

    public function scopePrevious($query)
    {
        return $query->whereIn('status', [
            'delivered', 'completed', 'cancelled',
        ]);
    }

    public function scopeToday($query)
    {
        return $query->whereDate('created_at', today());
    }

    // ─── Accessors ───────────────────────────────────────────

    public function getCanCancelAttribute(): bool
    {
        if ($this->status === 'pending') return true;

        if ($this->status === 'confirmed' && $this->cancellation_deadline) {
            return now()->isBefore($this->cancellation_deadline);
        }

        return false;
    }

    public function getStatusLabelArAttribute(): string
    {
        return match ($this->status) {
            'pending' => 'بانتظار التأكيد',
            'confirmed' => 'مؤكد',
            'preparing' => 'قيد التحضير',
            'ready' => 'جاهز',
            'out_for_delivery' => 'في الطريق',
            'delivered' => 'تم التوصيل',
            'completed' => 'مكتمل',
            'cancelled' => 'ملغي',
            default => $this->status,
        };
    }

    public function getStatusLabelEnAttribute(): string
    {
        return match ($this->status) {
            'pending' => 'Pending',
            'confirmed' => 'Confirmed',
            'preparing' => 'Preparing',
            'ready' => 'Ready',
            'out_for_delivery' => 'Out for Delivery',
            'delivered' => 'Delivered',
            'completed' => 'Completed',
            'cancelled' => 'Cancelled',
            default => $this->status,
        };
    }

    public function getHasReviewAttribute(): bool
    {
        return $this->rating !== null;
    }

    public function getNeedsDeliveryConfirmationAttribute(): bool
    {
        if ($this->delivery_confirmed_at) return false;
        if (!in_array($this->status, ['confirmed', 'preparing', 'ready', 'out_for_delivery'])) return false;
        if (!$this->confirmed_at) return false;

        // 40 minutes after confirmation
        return now()->diffInMinutes($this->confirmed_at) >= 40;
    }

    // ─── Methods ─────────────────────────────────────────────

    public static function generateOrderNumber(): string
    {
        $date = now()->format('Ymd');
        $lastOrder = static::where('order_number', 'like', "IBN-{$date}-%")
            ->orderByDesc('id')
            ->first();

        $sequence = 1;
        if ($lastOrder) {
            $lastSequence = (int) substr($lastOrder->order_number, -4);
            $sequence = $lastSequence + 1;
        }

        return sprintf("IBN-%s-%04d", $date, $sequence);
    }

    public function confirm(int $estimatedMinutes, bool $autoConfirmed = false): void
    {
        $this->update([
            'status' => 'confirmed',
            'confirmed_at' => now(),
            'estimated_time' => $estimatedMinutes,
            'cancellation_deadline' => now()->addMinutes(3),
            'auto_confirmed' => $autoConfirmed,
        ]);
    }

    public function cancel(?string $reason = null): bool
    {
        if (!$this->can_cancel) return false;

        $this->update([
            'status' => 'cancelled',
            'cancel_reason' => $reason,
        ]);

        return true;
    }

    public function confirmDelivery(): void
    {
        $this->update([
            'status' => 'delivered',
            'delivery_confirmed_at' => now(),
        ]);
    }

    public function submitReview(int $rating, ?string $note = null): void
    {
        $this->update([
            'rating' => $rating,
            'review_note' => $note,
            'status' => 'completed',
        ]);
    }
}
