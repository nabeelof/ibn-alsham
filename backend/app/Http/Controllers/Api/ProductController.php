<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Category;
use App\Models\Product;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class ProductController extends Controller
{
    use ApiResponse;

    public function categories()
    {
        $locale = app()->getLocale();

        $categories = Category::where('is_active', true)
            ->whereNull('parent_id')
            ->with(['children' => fn ($q) => $q->withCount(['products' => fn ($p) => $p->where('is_available', true)])])
            ->withCount(['products' => fn ($q) => $q->where('is_available', true)])
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'parent_id' => null,
                'name' => $c->{"name_{$locale}"},
                'icon' => $c->image ? asset("storage/{$c->image}") : null,
                'products_count' => $c->products_count,
                'children' => $c->children->map(fn ($child) => [
                    'id' => $child->id,
                    'parent_id' => $c->id,
                    'name' => $child->{"name_{$locale}"},
                    'icon' => $child->image ? asset("storage/{$child->image}") : null,
                    'products_count' => $child->products_count,
                    'children' => [],
                ])->values(),
            ]);

        return $this->success($categories);
    }

    public function index(Request $request)
    {
        $locale = app()->getLocale();

        $products = Product::with('category')
            ->where('is_available', true)
            ->when($request->category_id, fn ($q, $id) => $q->where('category_id', $id))
            ->when($request->search, fn ($q, $s) => $q->where("name_{$locale}", 'like', "%{$s}%"))
            ->orderBy('sort_order')
            ->paginate(20);

        return $this->success(
            $products->map(fn ($p) => $this->formatProduct($p, $locale)),
            [
                'current_page' => $products->currentPage(),
                'last_page' => $products->lastPage(),
                'total' => $products->total(),
            ]
        );
    }

    public function featured(Request $request)
    {
        $locale = app()->getLocale();
        $products = Product::with('category')
            ->where('is_available', true)
            ->where('is_featured', true)
            ->orderByDesc('order_count')
            ->paginate(20);

        return $this->success(
            $products->map(fn ($p) => $this->formatProduct($p, $locale)),
            [
                'current_page' => $products->currentPage(),
                'last_page' => $products->lastPage(),
                'total' => $products->total(),
            ]
        );
    }

    public function bestSeller()
    {
        $locale = app()->getLocale();
        $products = Product::with('category')
            ->where('is_available', true)
            ->orderByDesc('order_count')
            ->take(20)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        return $this->success($products);
    }

    public function todaysDeal()
    {
        $locale = app()->getLocale();
        $products = Product::with('category')
            ->where('is_available', true)
            ->whereNotNull('discounted_price')
            ->take(20)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        return $this->success($products);
    }

    /**
     * Search with the advanced filters used by the app's results screen:
     * free text, category (including its subcategories), JOD price range,
     * offers-only, and sort order.
     */
    public function search(Request $request)
    {
        $locale = app()->getLocale();
        $search = $request->input('name') ?? $request->input('query');

        $minPrice = $request->filled('min_price') ? (float) $request->input('min_price') : null;
        $maxPrice = $request->filled('max_price') ? (float) $request->input('max_price') : null;

        $query = Product::with('category')
            ->where('is_available', true)
            ->when($search, fn ($q) => $q->search($search))
            ->when($request->filled('category_id'), fn ($q) => $q->inCategoryTree((int) $request->input('category_id')))
            ->priceBetween($minPrice, $maxPrice)
            ->when($request->boolean('only_offers'), fn ($q) => $q->withDiscount())
            ->when($request->boolean('only_featured'), fn ($q) => $q->featured());

        match ($request->input('sort', 'default')) {
            'price_asc' => $query->orderBy('price'),
            'price_desc' => $query->orderByDesc('price'),
            'newest' => $query->orderByDesc('created_at'),
            'best_selling' => $query->orderByDesc('order_count'),
            default => $query->orderBy('sort_order'),
        };

        $products = $query->paginate((int) $request->input('per_page', 20));

        return $this->success(
            $products->map(fn ($p) => $this->formatProduct($p, $locale)),
            [
                'current_page' => $products->currentPage(),
                'last_page' => $products->lastPage(),
                'total' => $products->total(),
            ]
        );
    }

    /**
     * Bounds and options the filter sheet needs to render itself.
     */
    public function filterOptions()
    {
        $locale = app()->getLocale();

        $categories = Category::where('is_active', true)
            ->whereNull('parent_id')
            ->with('children')
            ->orderBy('sort_order')
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'name' => $c->{"name_{$locale}"},
                'children' => $c->children->map(fn ($child) => [
                    'id' => $child->id,
                    'name' => $child->{"name_{$locale}"},
                ])->values(),
            ]);

        return $this->success([
            'categories' => $categories,
            'min_price' => (float) (Product::where('is_available', true)->min('price') ?? 0),
            'max_price' => (float) (Product::where('is_available', true)->max('price') ?? 0),
            'currency' => 'JOD',
            'sorts' => ['default', 'price_asc', 'price_desc', 'newest', 'best_selling'],
        ]);
    }

    public function searchSuggestions(Request $request)
    {
        $locale = app()->getLocale();
        $query = $request->input('query_key');
        if (!$query) {
            return $this->success([]);
        }

        $suggestions = Product::where('is_available', true)
            ->where("name_{$locale}", 'like', "%{$query}%")
            ->orderBy('sort_order')
            ->limit(10)
            ->pluck("name_{$locale}")
            ->values();

        return $this->success($suggestions);
    }

    public function show(Product $product)
    {
        $locale = app()->getLocale();

        if (!$product->is_available) {
            return $this->error('هذا المنتج غير متاح حالياً', 404);
        }

        $product->load([
            'category',
            'optionGroups.options' => fn ($q) => $q->where('is_active', true)->orderBy('sort_order'),
        ]);

        return $this->success([
            'id' => $product->id,
            'name' => $product->{"name_{$locale}"},
            'description' => $product->{"description_{$locale}"},
            'price' => (float) $product->price,
            'discounted_price' => $product->discounted_price ? (float) $product->discounted_price : null,
            'image_url' => $product->image ? asset("storage/{$product->image}") : null,
            'category' => [
                'id' => $product->category?->id,
                'name' => $product->category?->{"name_{$locale}"},
            ],
            'option_groups' => $product->optionGroups->map(fn ($group) => [
                'id' => $group->id,
                'name' => $group->{"name_{$locale}"},
                'type' => $group->type,
                'is_required' => $group->is_required,
                'is_skippable' => $group->is_skippable,
                'min_selections' => $group->min_selections,
                'max_selections' => $group->max_selections,
                'options' => $group->options->map(fn ($o) => [
                    'id' => $o->id,
                    'name' => $o->{"name_{$locale}"},
                    'price' => (float) $o->price,
                    'default_selected' => $o->default_selected,
                    'is_user_changeable' => $o->is_user_changeable,
                ]),
            ]),
        ]);
    }

    public function toggleFavorite(Product $product)
    {
        $user = request()->user();
        $user->favorites()->toggle($product->id);
        $isFavorite = $user->favorites()->where('product_id', $product->id)->exists();
        return $this->success(['is_favorite' => $isFavorite]);
    }

    public function favorites()
    {
        $locale = app()->getLocale();
        $products = request()->user()->favorites()
            ->where('is_available', true)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        return $this->success($products);
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
            'is_available' => $product->is_available,
        ];
    }

    public function categoryProducts(int $id)
    {
        $locale = app()->getLocale();
        $products = Product::with('category')
            ->where('is_available', true)
            ->inCategoryTree($id)
            ->orderBy('sort_order')
            ->paginate(20);

        return $this->success(
            $products->map(fn ($p) => $this->formatProduct($p, $locale)),
            [
                'current_page' => $products->currentPage(),
                'last_page' => $products->lastPage(),
                'total' => $products->total(),
            ]
        );
    }

    public function brandProducts(string $slug)
    {
        return $this->success([]);
    }

    public function inhouse(Request $request)
    {
        $locale = app()->getLocale();
        $products = Product::with('category')
            ->where('is_available', true)
            ->orderBy('sort_order')
            ->paginate(20);

        return $this->success(
            $products->map(fn ($p) => $this->formatProduct($p, $locale)),
            [
                'current_page' => $products->currentPage(),
                'last_page' => $products->lastPage(),
                'total' => $products->total(),
            ]
        );
    }

    public function lastViewed(Request $request)
    {
        $locale = app()->getLocale();
        $products = Product::with('category')
            ->where('is_available', true)
            ->orderByDesc('updated_at')
            ->take(10)
            ->get()
            ->map(fn ($p) => $this->formatProduct($p, $locale));

        return $this->success($products);
    }

    public function frequentlyBought(string $slug)
    {
        return $this->success([]);
    }

    public function topFromSeller(string $slug)
    {
        return $this->success([]);
    }

    public function variantPrice(Request $request)
    {
        $slug = $request->input('slug');
        $quantity = (int) $request->input('quantity', 1);
        $product = $this->findBySlug($slug);

        if (!$product) {
            return response()->json(['result' => false, 'data' => null], 404);
        }

        $price = $product->discounted_price ?? $product->price;

        return response()->json([
            'result' => true,
            'data' => [
                'price' => number_format((float) $price, 3, '.', ''),
                'stock' => 999,
                'stock_txt' => 'In stock',
                'digital' => 0,
                'variant' => null,
                'variation' => null,
                'max_limit' => 99,
                'in_stock' => 1,
                'image' => $product->image_url,
            ],
        ]);
    }

    public function varientPrice(Request $request)
    {
        $id = $request->input('id');
        $quantity = (int) $request->input('quantity', 1);
        $product = Product::find($id);
        if (!$product) {
            return response()->json(['result' => false, 'data' => null], 404);
        }

        $price = $product->discounted_price ?? $product->price;

        return response()->json([
            'result' => true,
            'data' => [
                'price' => number_format((float) $price, 3, '.', ''),
                'quantity' => $quantity,
                'digital' => 0,
                'variation' => null,
                'max_limit' => 99,
                'in_stock' => 1,
            ],
        ]);
    }

    public function detailsBySlug(string $slug, string $userId)
    {
        $product = $this->findBySlug($slug);
        if (!$product) {
            return response()->json(['data' => [], 'success' => false, 'status' => 404], 404);
        }

        $locale = app()->getLocale();
        $product->loadMissing([
            'images',
            'optionGroups.options' => fn ($q) => $q->where('is_active', true)->orderBy('sort_order'),
        ]);
        $mainPrice = $product->discounted_price ?? $product->price;
        $strokedPrice = $product->discounted_price ? $product->price : null;

        $details = [
            'id' => $product->id,
            'name' => $product->{"name_{$locale}"},
            'added_by' => 'admin',
            'seller_id' => null,
            'shop_id' => null,
            'shop_slug' => null,
            'shop_name' => null,
            'shop_logo' => null,
            'photos' => $product->images->map(fn ($img) => [
                'variant' => null,
                'path' => $img->image ? asset("storage/{$img->image}") : null,
            ])->values(),
            'thumbnail_image' => $product->image ? asset("storage/{$product->image}") : null,
            'tags' => [],
            'price_high_low' => number_format((float) $mainPrice, 3, '.', ''),
            'choice_options' => [],
            'option_groups' => $product->optionGroups->map(fn ($group) => [
                'id' => $group->id,
                'name' => $group->{"name_{$locale}"},
                'type' => $group->type,
                'is_required' => $group->is_required,
                'is_skippable' => $group->is_skippable,
                'min_selections' => $group->min_selections,
                'max_selections' => $group->max_selections,
                'options' => $group->options->map(fn ($o) => [
                    'id' => $o->id,
                    'name' => $o->{"name_{$locale}"},
                    'price' => (float) $o->price,
                    'default_selected' => $o->default_selected,
                    'is_user_changeable' => $o->is_user_changeable,
                ]),
            ]),
            'colors' => [],
            'has_discount' => $product->discounted_price ? true : false,
            'discount' => $product->discount_percentage ?? 0,
            'stroked_price' => $strokedPrice ? number_format((float) $strokedPrice, 3, '.', '') : null,
            'main_price' => number_format((float) $mainPrice, 3, '.', ''),
            'calculable_price' => (float) $mainPrice,
            'currency_symbol' => 'JOD',
            'current_stock' => 999,
            'unit' => 'pcs',
            'rating' => 0,
            'rating_count' => 0,
            'earn_point' => 0,
            'description' => $product->{"description_{$locale}"} ?? '',
            'downloads' => null,
            'video_link' => [],
            'videos' => [],
            'link' => null,
            'brand' => null,
            'wholesale' => [],
            'est_shipping_time' => null,
        ];

        return response()->json([
            'data' => [$details],
            'success' => true,
            'status' => 200,
        ]);
    }

    private function findBySlug(?string $slug): ?Product
    {
        if (!$slug) return null;
        if (ctype_digit($slug)) {
            return Product::find((int) $slug);
        }

        return Product::where('name_en', $slug)
            ->orWhere('name_ar', $slug)
            ->first();
    }
}
