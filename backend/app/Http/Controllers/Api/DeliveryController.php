<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\DeliveryService;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class DeliveryController extends Controller
{
    use ApiResponse;

    public function __construct(private DeliveryService $deliveryService) {}

    public function calculate(Request $request)
    {
        $request->validate([
            'latitude' => 'required|numeric|between:-90,90',
            'longitude' => 'required|numeric|between:-180,180',
        ]);

        $result = $this->deliveryService->calculate(
            $request->float('latitude'),
            $request->float('longitude')
        );

        if (!$result['is_deliverable']) {
            return $this->error('عذراً، موقعك خارج نطاق التوصيل حالياً.', 422, [
                'distance_km' => $result['distance_km'],
                'max_km' => $result['max_km'],
            ]);
        }

        return $this->success([
            'is_deliverable' => true,
            'delivery_fee' => $result['delivery_fee'],
            'distance_km' => $result['distance_km'],
            'estimated_minutes' => $result['estimated_minutes'],
        ]);
    }

    public function info()
    {
        return $this->success([
            'supports_delivery' => true,
            'supports_pickup' => true,
        ]);
    }

    public function shippingCost(Request $request)
    {
        $request->validate([
            'lat' => 'nullable|numeric',
            'lng' => 'nullable|numeric',
        ]);

        if (!$request->input('lat') || !$request->input('lng')) {
            return $this->success(['shipping_cost' => 0]);
        }

        $result = $this->deliveryService->calculate(
            $request->float('lat'),
            $request->float('lng')
        );

        return $this->success(['shipping_cost' => $result['delivery_fee'] ?? 0]);
    }
}
