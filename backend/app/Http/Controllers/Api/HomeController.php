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
            ->whereNull('parent_id')
            ->with('children')
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'parent_id' => null,
                'name' => $c->{"name_{$locale}"},
                'icon' => $c->image ? asset("storage/{$c->image}") : null,
                'children' => $c->children->map(fn ($child) => [
                    'id' => $child->id,
                    'parent_id' => $c->id,
                    'name' => $child->{"name_{$locale}"},
                    'icon' => $child->image ? asset("storage/{$child->image}") : null,
                    'children' => [],
                ])->values(),
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
            ->withDiscount()
            ->take(8)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        $mostOrdered = Product::with('category')
            ->where('is_available', true)
            ->where('order_count', '>', 0)
            ->orderByDesc('order_count')
            ->take(8)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        $settings = Setting::pluck("value_{$locale}", 'key');

        return $this->success([
            'banners' => $banners,
            'categories' => $categories,
            'featured_products' => $featuredProducts,
            'special_offers' => $specialOffers,
            'most_ordered' => $mostOrdered,
            'active_order' => $this->activeOrder($locale),
            'announcement' => $settings->get('announcement'),
        ]);
    }

    /**
     * The order status card shown at the top of home, or null when the
     * caller is a guest or has nothing in flight.
     */
    private function activeOrder(string $locale): ?array
    {
        // /home is a public route, so the default guard never resolves a
        // bearer token — ask Sanctum directly.
        $user = auth('sanctum')->user();
        if (!$user) {
            return null;
        }

        $order = $user->orders()->current()->latest()->first();
        if (!$order) {
            return null;
        }

        return [
            'id' => $order->id,
            'order_number' => $order->order_number,
            'type' => $order->type,
            'status' => $order->status,
            'status_label' => $locale === 'ar' ? $order->status_label_ar : $order->status_label_en,
            'total' => (float) $order->total,
            'estimated_time' => $order->estimated_time,
            'created_at' => $order->created_at->format('Y-m-d H:i'),
        ];
    }

    private function formatProduct($product, string $locale): array
    {
        return [
            'id' => $product->id,
            'name' => $product->{"name_{$locale}"},
            'description' => $product->{"description_{$locale}"},
            'price' => (float) $product->price,
            'discounted_price' => $product->has_discount ? (float) $product->discounted_price : null,
            'discount_type' => $product->discount_type ?? 'none',
            'discount_value' => $product->discount_value !== null ? (float) $product->discount_value : null,
            'discount_percentage' => $product->discount_percentage,
            'has_discount' => $product->has_discount,
            'offer_ends_at' => $product->offer_ends_at?->toIso8601String(),
            'currency_symbol' => 'JOD',
            'image_url' => $product->image ? asset("storage/{$product->image}") : null,
            'category_id' => $product->category_id,
            'category_name' => $product->category?->{"name_{$locale}"},
        ];
    }
}
