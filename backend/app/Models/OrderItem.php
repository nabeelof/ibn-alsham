<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class OrderItem extends Model
{
    protected $fillable = [
        'order_id',
        'product_id',
        'product_name_ar',
        'product_name_en',
        'quantity',
        'unit_price',
        'discount_price',
        'options_total',
        'total',
    ];

    protected function casts(): array
    {
        return [
            'unit_price' => 'decimal:3',
            'discount_price' => 'decimal:3',
            'options_total' => 'decimal:3',
            'total' => 'decimal:3',
        ];
    }

    public function order()
    {
        return $this->belongsTo(Order::class);
    }

    public function product()
    {
        return $this->belongsTo(Product::class);
    }

    public function options()
    {
        return $this->hasMany(OrderItemOption::class);
    }

    public function getProductNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->product_name_ar : $this->product_name_en;
    }
}
