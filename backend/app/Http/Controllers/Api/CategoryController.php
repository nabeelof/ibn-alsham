<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Category;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;

class CategoryController extends Controller
{
    use ApiResponse;

    /**
     * Get all active categories.
     */
    public function index(): JsonResponse
    {
        $locale = app()->getLocale();

        $categories = Category::active()->roots()->ordered()
            ->with(['children' => fn ($q) => $q->withCount('activeProducts')])
            ->withCount('activeProducts')
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'parent_id' => null,
                'name' => $locale === 'ar' ? $c->name_ar : $c->name_en,
                'image' => $c->image_url,
                'products_count' => $c->active_products_count,
                'children' => $c->children->map(fn ($child) => [
                    'id' => $child->id,
                    'parent_id' => $c->id,
                    'name' => $locale === 'ar' ? $child->name_ar : $child->name_en,
                    'image' => $child->image_url,
                    'products_count' => $child->active_products_count,
                    'children' => [],
                ])->values(),
            ]);

        return $this->success($categories);
    }

    /**
     * Subcategories of a category.
     */
    public function children(int $id): JsonResponse
    {
        $locale = app()->getLocale();

        $children = Category::where('parent_id', $id)
            ->where('is_active', true)
            ->withCount('activeProducts')
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'parent_id' => $id,
                'name' => $locale === 'ar' ? $c->name_ar : $c->name_en,
                'image' => $c->image_url,
                'products_count' => $c->active_products_count,
                'children' => [],
            ]);

        return $this->success($children);
    }

    /**
     * Get products in a specific category.
     */
    public function products(int $categoryId): JsonResponse
    {
        $locale = app()->getLocale();
        $userId = auth('sanctum')->id();

        $category = Category::findOrFail($categoryId);

        $products = \App\Models\Product::where('is_available', true)
            ->inCategoryTree($categoryId)
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($p) => [
                'id' => $p->id,
                'name' => $locale === 'ar' ? $p->name_ar : $p->name_en,
                'description' => $locale === 'ar' ? $p->description_ar : $p->description_en,
                'price' => $p->price,
                'discounted_price' => $p->discounted_price,
                'has_discount' => $p->has_discount,
                'discount_percentage' => $p->discount_percentage,
                'image' => $p->image_url,
                'is_featured' => $p->is_featured,
                'order_count' => $p->order_count,
                'is_favorite' => $userId ? $p->favoritedBy()->where('user_id', $userId)->exists() : false,
            ]);

        return $this->success([
            'category' => [
                'id' => $category->id,
                'name' => $locale === 'ar' ? $category->name_ar : $category->name_en,
                'image' => $category->image_url,
            ],
            'products' => $products,
        ]);
    }

    public function featured(): JsonResponse
    {
        $locale = app()->getLocale();
        $categories = Category::active()->ordered()
            ->take(12)
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'name' => $locale === 'ar' ? $c->name_ar : $c->name_en,
                'banner' => $c->image_url,
            ]);

        return $this->success($categories);
    }

    public function top(): JsonResponse
    {
        $locale = app()->getLocale();
        $categories = Category::active()->ordered()
            ->take(12)
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'name' => $locale === 'ar' ? $c->name_ar : $c->name_en,
                'banner' => $c->image_url,
            ]);

        return $this->success($categories);
    }

    public function info(string $slug): JsonResponse
    {
        $locale = app()->getLocale();
        $category = ctype_digit($slug) ? Category::findOrFail((int) $slug) : Category::where('name_en', $slug)
            ->orWhere('name_ar', $slug)
            ->firstOrFail();

        return $this->success([
            'id' => $category->id,
            'name' => $locale === 'ar' ? $category->name_ar : $category->name_en,
            'banner' => $category->image_url,
        ]);
    }

    public function filterList(): JsonResponse
    {
        $locale = app()->getLocale();
        $categories = Category::active()->ordered()
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'name' => $locale === 'ar' ? $c->name_ar : $c->name_en,
            ]);

        return $this->success($categories);
    }
}
