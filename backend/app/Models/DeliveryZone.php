<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class DeliveryZone extends Model
{
    protected $fillable = [
        'name_ar',
        'name_en',
        'min_km',
        'max_km',
        'price_per_km',
        'base_price',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'min_km' => 'decimal:2',
            'max_km' => 'decimal:2',
            'price_per_km' => 'decimal:3',
            'base_price' => 'decimal:3',
            'is_active' => 'boolean',
        ];
    }

    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }
}
