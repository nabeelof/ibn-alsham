<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Coupon;
use App\Services\CouponService;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class CouponController extends Controller
{
    use ApiResponse;

    public function __construct(
        private CouponService $coupons,
    ) {}

    /**
     * Validate a coupon code against the current cart subtotal and return the
     * JOD discount it would produce. Does not consume the coupon — that happens
     * when the order is placed.
     */
    public function apply(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'coupon_code' => 'required|string|max:64',
            'subtotal' => 'required|numeric|min:0',
        ]);

        if ($validator->fails()) {
            return $this->error(__('validations.validation_error'), 422, $validator->errors());
        }

        $result = $this->coupons->validate(
            $request->input('coupon_code'),
            auth('sanctum')->id(),
            (float) $request->input('subtotal'),
        );

        if (!$result['ok']) {
            return $this->error($result['message'] ?? __('coupons.not_found'), 422);
        }

        return $this->success(
            $this->coupons->present($result['coupon'], $result['discount']),
            message: __('coupons.applied'),
        );
    }

    public function remove(Request $request): JsonResponse
    {
        return $this->success(['removed' => true], message: __('coupons.removed'));
    }

    /**
     * Coupons the caller can currently use — the "offers" list in the app.
     */
    public function list(Request $request): JsonResponse
    {
        $userId = auth('sanctum')->id();
        $subtotal = (float) $request->input('subtotal', 0);

        $coupons = Coupon::active()
            ->orderBy('min_order_total')
            ->get()
            ->filter(fn (Coupon $c) => $c->rejectionReason($userId, max($subtotal, (float) $c->min_order_total)) === null)
            ->map(fn (Coupon $c) => $this->coupons->present($c, $c->discountFor($subtotal)))
            ->values();

        return $this->success($coupons);
    }

    public function products(int $id): JsonResponse
    {
        // Coupons in this app apply to the whole order, not to product subsets.
        return $this->success([]);
    }
}
