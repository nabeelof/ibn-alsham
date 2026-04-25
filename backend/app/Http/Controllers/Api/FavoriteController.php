<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Product;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class FavoriteController extends Controller
{
    use ApiResponse;

    /**
     * Get user's favorite products.
     */
    public function index(Request $request): JsonResponse
    {
        $locale = app()->getLocale();
        $user = $request->user();

        $favorites = $user->favorites()
            ->with('category')
            ->orderByPivot('created_at', 'desc')
            ->get()
            ->map(fn ($p) => [
                'id' => $p->id,
                'name' => $locale === 'ar' ? $p->name_ar : $p->name_en,
                'price' => $p->price,
                'discounted_price' => $p->discounted_price,
                'has_discount' => $p->has_discount,
                'image' => $p->image_url,
                'is_favorite' => true,
                'category' => $p->category ? [
                    'id' => $p->category->id,
                    'name' => $locale === 'ar' ? $p->category->name_ar : $p->category->name_en,
                ] : null,
            ]);

        return $this->success($favorites);
    }

    /**
     * Toggle favorite status for a product.
     */
    public function toggle(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'product_id' => 'required|exists:products,id',
        ]);

        if ($validator->fails()) {
            return $this->error(__('validations.validation_error'), 422, $validator->errors());
        }

        $user = $request->user();
        $productId = $request->product_id;

        $exists = $user->favorites()->where('product_id', $productId)->exists();

        if ($exists) {
            $user->favorites()->detach($productId);
            $isFavorite = false;
            $message = __('favorites.removed');
        } else {
            $user->favorites()->attach($productId);
            $isFavorite = true;
            $message = __('favorites.added');
        }

        return $this->success(['is_favorite' => $isFavorite], $message);
    }
}
