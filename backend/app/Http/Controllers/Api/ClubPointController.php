<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;

class ClubPointController extends Controller
{
    public function list(): JsonResponse
    {
        return response()->json([
            'data' => [],
            'result' => true,
            'status' => 200,
        ]);
    }

    public function convert(): JsonResponse
    {
        return response()->json([
            'result' => false,
            'message' => 'Club point conversion not configured',
        ]);
    }
}
