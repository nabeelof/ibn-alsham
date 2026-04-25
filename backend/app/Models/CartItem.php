<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CartItem extends Model
{
    use HasFactory;

    protected $fillable = [
        'cart_id',
        'product_id',
        'quantity',
        'variant',
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

    public function cart()
    {
        return $this->belongsTo(Cart::class);
    }

    public function product()
    {
        return $this->belongsTo(Product::class);
    }
}
