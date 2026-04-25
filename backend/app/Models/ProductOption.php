<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ProductOption extends Model
{
    protected $fillable = [
        'product_id',
        'option_group_id',
        'name_ar',
        'name_en',
        'price',
        'is_required',
        'default_selected',
        'is_user_changeable',
        'is_active',
        'sort_order',
    ];

    protected function casts(): array
    {
        return [
            'price' => 'decimal:3',
            'is_required' => 'boolean',
            'default_selected' => 'boolean',
            'is_user_changeable' => 'boolean',
            'is_active' => 'boolean',
        ];
    }

    public function product()
    {
        return $this->belongsTo(Product::class);
    }

    public function optionGroup()
    {
        return $this->belongsTo(ProductOptionGroup::class, 'option_group_id');
    }

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }

    public function getIsFreeAttribute(): bool
    {
        return (float) $this->price === 0.0;
    }
}
