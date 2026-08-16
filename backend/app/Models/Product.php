<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Product extends Model
{
    use HasFactory;

    protected $fillable = [
        'category_id',
        'name_ar',
        'name_en',
        'description_ar',
        'description_en',
        'price',
        'cost_price',
        'discounted_price',
        'discount_type',
        'discount_value',
        'offer_starts_at',
        'offer_ends_at',
        'image',
        'is_featured',
        'is_available',
        'order_count',
        'sort_order',
    ];

    protected function casts(): array
    {
        return [
            'price' => 'decimal:3',
            'cost_price' => 'decimal:3',
            'discounted_price' => 'decimal:3',
            'discount_value' => 'decimal:3',
            'offer_starts_at' => 'datetime',
            'offer_ends_at' => 'datetime',
            'is_featured' => 'boolean',
            'is_available' => 'boolean',
        ];
    }

    // ─── Relationships ───────────────────────────────────────

    public function category()
    {
        return $this->belongsTo(Category::class);
    }

    public function images()
    {
        return $this->hasMany(ProductImage::class)->orderBy('sort_order');
    }

    public function options()
    {
        return $this->hasMany(ProductOption::class)->where('is_active', true)->orderBy('sort_order');
    }

    public function allOptions()
    {
        return $this->hasMany(ProductOption::class)->orderBy('sort_order');
    }

    public function optionGroups()
    {
        return $this->hasMany(ProductOptionGroup::class)
            ->where('is_active', true)
            ->orderBy('sort_order');
    }

    public function allOptionGroups()
    {
        return $this->hasMany(ProductOptionGroup::class)->orderBy('sort_order');
    }

    public function favoritedBy()
    {
        return $this->belongsToMany(User::class, 'favorites')->withTimestamps();
    }

    // ─── Scopes ──────────────────────────────────────────────

    public function scopeActive($query)
    {
        return $query->where('is_available', true);
    }

    public function scopeFeatured($query)
    {
        return $query->where('is_featured', true);
    }

    public function scopeMostOrdered($query)
    {
        return $query->orderByDesc('order_count');
    }

    public function scopeWithDiscount($query)
    {
        return $query->whereNotNull('discounted_price')
            ->where('discounted_price', '>', 0)
            ->where(fn ($q) => $q->whereNull('offer_starts_at')->orWhere('offer_starts_at', '<=', now()))
            ->where(fn ($q) => $q->whereNull('offer_ends_at')->orWhere('offer_ends_at', '>=', now()));
    }

    public function scopePriceBetween($query, ?float $min, ?float $max)
    {
        return $query
            ->when($min !== null, fn ($q) => $q->where('price', '>=', $min))
            ->when($max !== null, fn ($q) => $q->where('price', '<=', $max));
    }

    /**
     * Products in the given category, including everything under its subcategories.
     */
    public function scopeInCategoryTree($query, int $categoryId)
    {
        $ids = Category::where('id', $categoryId)
            ->orWhere('parent_id', $categoryId)
            ->pluck('id');

        return $query->whereIn('category_id', $ids);
    }

    public function scopeSearch($query, string $search)
    {
        return $query->where(function ($q) use ($search) {
            $q->where('name_ar', 'like', "%{$search}%")
              ->orWhere('name_en', 'like', "%{$search}%")
              ->orWhere('description_ar', 'like', "%{$search}%")
              ->orWhere('description_en', 'like', "%{$search}%");
        });
    }

    // ─── Accessors ───────────────────────────────────────────

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }

    public function getDescriptionAttribute(): ?string
    {
        return app()->getLocale() === 'ar' ? $this->description_ar : $this->description_en;
    }

    public function getEffectivePriceAttribute(): float
    {
        return $this->has_discount
            ? (float) $this->discounted_price
            : (float) $this->price;
    }

    public function getOfferIsLiveAttribute(): bool
    {
        if ($this->offer_starts_at && $this->offer_starts_at->isFuture()) {
            return false;
        }

        if ($this->offer_ends_at && $this->offer_ends_at->isPast()) {
            return false;
        }

        return true;
    }

    public function getHasDiscountAttribute(): bool
    {
        return $this->discounted_price !== null
            && $this->discounted_price > 0
            && $this->offer_is_live;
    }

    /**
     * Recompute discounted_price from discount_type/discount_value.
     * Call after the admin edits an offer so both representations stay in sync.
     */
    public function syncDiscountedPrice(): void
    {
        $price = (float) $this->price;
        $value = (float) ($this->discount_value ?? 0);

        $discounted = match ($this->discount_type) {
            'percent' => round($price - ($price * $value / 100), 3),
            'fixed' => round($price - $value, 3),
            default => null,
        };

        $this->discounted_price = ($discounted !== null && $discounted > 0 && $discounted < $price)
            ? $discounted
            : null;
    }

    public function getImageUrlAttribute(): ?string
    {
        return $this->image ? asset('storage/' . $this->image) : null;
    }

    public function getDiscountPercentageAttribute(): ?int
    {
        if (!$this->has_discount) return null;
        return (int) round((($this->price - $this->discounted_price) / $this->price) * 100);
    }

    // ─── Methods ─────────────────────────────────────────────

    public function incrementOrderCount(int $quantity = 1): void
    {
        $this->increment('order_count', $quantity);
    }
}
