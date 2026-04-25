<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;

class WalletController extends Controller
{
    public function balance(): JsonResponse
    {
        return response()->json([
            'balance' => '0.000',
            'currency_symbol' => 'JOD',
            'result' => true,
        ]);
    }

    public function history(): JsonResponse
    {
        return response()->json([
            'data' => [],
            'links' => null,
            'meta' => null,
            'result' => true,
            'status' => 200,
        ]);
    }

    public function offlineRecharge(): JsonResponse
    {
        return response()->json([
            'result' => false,
            'message' => 'Offline recharge not configured',
        ]);
    }
}
