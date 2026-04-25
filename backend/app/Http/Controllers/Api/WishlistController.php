<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Product;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;

class WishlistController extends Controller
{
    use ApiResponse;

    public function index(): JsonResponse
    {
        $user = request()->user();
        $locale = app()->getLocale();

        $products = $user->favorites()
            ->where('is_available', true)
            ->get()
            ->map(fn ($p) => [
                'id' => $p->id,
                'slug' => (string) $p->id,
                'name' => $p->{"name_{$locale}"},
                'thumbnail_image' => $p->image_url,
                'main_price' => number_format((float) ($p->discounted_price ?? $p->price), 3, '.', ''),
                'stroked_price' => $p->discounted_price ? number_format((float) $p->price, 3, '.', '') : null,
                'has_discount' => $p->has_discount,
                'discount' => $p->discount_percentage ?? 0,
                'rating' => 0,
                'sales' => $p->order_count,
                'links' => [
                    'details' => null,
                ],
                'is_wholesale' => false,
            ])
            ->values();

        return response()->json([
            'data' => $products,
            'success' => true,
            'status' => 200,
        ]);
    }

    public function add(string $slug): JsonResponse
    {
        $user = request()->user();
        $product = $this->findBySlug($slug);
        if (!$product) {
            return response()->json(['result' => false, 'message' => 'Product not found'], 404);
        }

        $user->favorites()->syncWithoutDetaching([$product->id]);

        return response()->json(['result' => true, 'message' => 'Added to wishlist']);
    }

    public function remove(string $slug): JsonResponse
    {
        $user = request()->user();
        $product = $this->findBySlug($slug);
        if (!$product) {
            return response()->json(['result' => false, 'message' => 'Product not found'], 404);
        }

        $user->favorites()->detach($product->id);

        return response()->json(['result' => true, 'message' => 'Removed from wishlist']);
    }

    public function check(string $slug): JsonResponse
    {
        $user = request()->user();
        $product = $this->findBySlug($slug);
        if (!$product) {
            return response()->json(['result' => false, 'message' => 'Product not found'], 404);
        }

        $exists = $user->favorites()->where('product_id', $product->id)->exists();

        return response()->json([
            'result' => true,
            'is_in_wishlist' => $exists,
            'message' => '',
        ]);
    }

    public function delete(int $wishlistId): JsonResponse
    {
        $user = request()->user();
        $user->favorites()->detach($wishlistId);

        return response()->json(['result' => true, 'message' => 'Deleted']);
    }

    private function findBySlug(string $slug): ?Product
    {
        if (ctype_digit($slug)) {
            return Product::find((int) $slug);
        }

        return Product::where('name_en', $slug)
            ->orWhere('name_ar', $slug)
            ->first();
    }
}
