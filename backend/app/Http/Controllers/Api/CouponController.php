<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CouponController extends Controller
{
    public function apply(Request $request): JsonResponse
    {
        return response()->json([
            'result' => false,
            'message' => 'Coupons are not configured yet',
        ]);
    }

    public function remove(Request $request): JsonResponse
    {
        return response()->json([
            'result' => true,
            'message' => 'Coupon removed',
        ]);
    }

    public function list(): JsonResponse
    {
        return response()->json([
            'data' => [],
            'links' => null,
            'meta' => null,
            'success' => true,
            'status' => 200,
        ]);
    }

    public function products(int $id): JsonResponse
    {
        return response()->json([
            'data' => [],
            'success' => true,
            'status' => 200,
        ]);
    }
}
