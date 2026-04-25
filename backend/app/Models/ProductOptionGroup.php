<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ProductOptionGroup extends Model
{
    protected $fillable = [
        'product_id',
        'name_ar',
        'name_en',
        'type',
        'is_required',
        'is_skippable',
        'min_selections',
        'max_selections',
        'sort_order',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'is_required' => 'boolean',
            'is_skippable' => 'boolean',
            'is_active' => 'boolean',
        ];
    }

    // ─── Relationships ───────────────────────────────────────

    public function product()
    {
        return $this->belongsTo(Product::class);
    }

    public function options()
    {
        return $this->hasMany(ProductOption::class, 'option_group_id')
            ->where('is_active', true)
            ->orderBy('sort_order');
    }

    public function allOptions()
    {
        return $this->hasMany(ProductOption::class, 'option_group_id')
            ->orderBy('sort_order');
    }

    // ─── Accessors ───────────────────────────────────────────

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }

    public function getIsCheckboxAttribute(): bool
    {
        return $this->type === 'checkbox';
    }

    public function getIsRadioAttribute(): bool
    {
        return $this->type === 'radio';
    }
}
