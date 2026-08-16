<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Category extends Model
{
    use HasFactory;

    protected $fillable = [
        'parent_id',
        'name_ar',
        'name_en',
        'image',
        'sort_order',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'is_active' => 'boolean',
        ];
    }

    // ─── Relationships ───────────────────────────────────────

    public function products()
    {
        return $this->hasMany(Product::class);
    }

    public function activeProducts()
    {
        return $this->hasMany(Product::class)->where('is_available', true);
    }

    public function parent()
    {
        return $this->belongsTo(Category::class, 'parent_id');
    }

    public function children()
    {
        return $this->hasMany(Category::class, 'parent_id')
            ->where('is_active', true)
            ->orderBy('sort_order');
    }

    // ─── Scopes ──────────────────────────────────────────────

    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }

    public function scopeOrdered($query)
    {
        return $query->orderBy('sort_order')->orderBy('name_ar');
    }

    public function scopeRoots($query)
    {
        return $query->whereNull('parent_id');
    }

    // ─── Accessors ───────────────────────────────────────────

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }

    public function getImageUrlAttribute(): ?string
    {
        return $this->image ? asset('storage/' . $this->image) : null;
    }
}
