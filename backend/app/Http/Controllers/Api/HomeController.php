<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Banner;
use App\Models\Category;
use App\Models\Product;
use App\Models\Setting;
use App\Traits\ApiResponse;

class HomeController extends Controller
{
    use ApiResponse;

    public function index()
    {
        $locale = app()->getLocale();

        $banners = Banner::where('is_active', true)
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($b) => [
                'id' => $b->id,
                'title' => $b->{"title_{$locale}"},
                'image_url' => $b->image ? asset("storage/{$b->image}") : null,
                'link_type' => $b->link_type,
                'link_value' => $b->link_value,
            ]);

        $categories = Category::where('is_active', true)
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'name' => $c->{"name_{$locale}"},
                'icon' => $c->image ? asset("storage/{$c->image}") : null,
            ]);

        $featuredProducts = Product::with('category')
            ->where('is_available', true)
            ->where('is_featured', true)
            ->orderByDesc('order_count')
            ->take(8)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        $specialOffers = Product::with('category')
            ->where('is_available', true)
            ->whereNotNull('discounted_price')
            ->take(8)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        $settings = Setting::pluck("value_{$locale}", 'key');

        return $this->success([
            'banners' => $banners,
            'categories' => $categories,
            'featured_products' => $featuredProducts,
            'special_offers' => $specialOffers,
            'announcement' => $settings->get('announcement'),
        ]);
    }

    private function formatProduct($product, string $locale): array
    {
        return [
            'id' => $product->id,
            'name' => $product->{"name_{$locale}"},
            'description' => $product->{"description_{$locale}"},
            'price' => (float) $product->price,
            'discounted_price' => $product->discounted_price ? (float) $product->discounted_price : null,
            'image_url' => $product->image ? asset("storage/{$product->image}") : null,
            'category_name' => $product->category?->{"name_{$locale}"},
        ];
    }
}
