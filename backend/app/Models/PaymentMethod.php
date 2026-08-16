<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PaymentMethod extends Model
{
    protected $fillable = [
        'key',
        'name_ar',
        'name_en',
        'description_ar',
        'description_en',
        'icon',
        'account_alias',
        'requires_reference',
        'is_online',
        'available_for_pickup',
        'available_for_delivery',
        'is_active',
        'sort_order',
    ];

    protected function casts(): array
    {
        return [
            'requires_reference' => 'boolean',
            'is_online' => 'boolean',
            'available_for_pickup' => 'boolean',
            'available_for_delivery' => 'boolean',
            'is_active' => 'boolean',
        ];
    }

    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }

    public function scopeForOrderType($query, ?string $type)
    {
        return match ($type) {
            'pickup' => $query->where('available_for_pickup', true),
            'delivery' => $query->where('available_for_delivery', true),
            default => $query,
        };
    }

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }

    public function getDescriptionAttribute(): ?string
    {
        return app()->getLocale() === 'ar' ? $this->description_ar : $this->description_en;
    }

    public function getIconUrlAttribute(): ?string
    {
        return $this->icon ? asset('storage/' . $this->icon) : null;
    }
}
