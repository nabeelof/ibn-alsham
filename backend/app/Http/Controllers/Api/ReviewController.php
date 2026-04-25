<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Order;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

class ReviewController extends Controller
{
    use ApiResponse;

    public function productReviews(int $productId): JsonResponse
    {
        $locale = app()->getLocale();

        // Get reviews from orders that contain this product
        $reviews = Order::whereHas('items', fn ($q) => $q->where('product_id', $productId))
            ->whereNotNull('rating')
            ->with('user')
            ->orderByDesc('updated_at')
            ->limit(50)
            ->get()
            ->map(fn ($order) => [
                'id' => $order->id,
                'user_name' => $order->user?->name ?? 'Anonymous',
                'rating' => $order->rating,
                'comment' => $order->review_note,
                'date' => $order->updated_at->format('Y-m-d'),
            ]);

        $avgRating = $reviews->avg('rating') ?? 0;

        return $this->success([
            'reviews' => $reviews,
            'average_rating' => round($avgRating, 1),
            'total_reviews' => $reviews->count(),
        ]);
    }

    public function submit(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'order_id' => 'required|exists:orders,id',
            'rating' => 'required|integer|min:1|max:5',
            'review_note' => 'nullable|string|max:500',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $user = $request->user();
        if (!$user) {
            return $this->error('Unauthorized', 401);
        }

        $order = $user->orders()->find($request->input('order_id'));
        if (!$order) {
            return $this->error('Order not found', 404);
        }

        if ($order->has_review) {
            return $this->error('Review already submitted', 400);
        }

        $order->submitReview(
            $request->input('rating'),
            $request->input('review_note')
        );

        return $this->success(null, 'Review submitted successfully');
    }
}
