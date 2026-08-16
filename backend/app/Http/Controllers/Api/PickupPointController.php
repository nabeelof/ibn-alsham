<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;

class PickupPointController extends Controller
{
    public function list(): JsonResponse
    {
        return response()->json([
            'data' => [],
            'success' => true,
            'status' => 200,
        ]);
    }
}
