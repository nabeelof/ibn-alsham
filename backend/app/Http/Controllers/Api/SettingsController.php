<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Setting;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;

class SettingsController extends Controller
{
    use ApiResponse;

    /**
     * Get public app settings.
     */
    public function index(): JsonResponse
    {
        $locale = app()->getLocale();
        
        $settings = Setting::all()->mapWithKeys(function ($setting) use ($locale) {
            return [$setting->key => $locale === 'ar' ? $setting->value_ar : $setting->value_en];
        });

        // Add some hardcoded constraints / system info
        $settings['currency'] = 'JOD';
        $settings['restaurant_lat'] = 31.9539;
        $settings['restaurant_lng'] = 35.9106;
        $settings['support_phone'] = $settings['contact_phone'] ?? '+962700000000';

        return $this->success($settings);
    }

    public function businessSettings(): JsonResponse
    {
        $settings = Setting::all()->map(fn ($s) => [
            'type' => $s->key,
            'value' => $s->value_en ?? $s->value_ar,
        ])->values();

        return response()->json([
            'data' => $settings,
            'success' => true,
            'status' => 200,
        ]);
    }

    public function languages(): JsonResponse
    {
        return response()->json([
            'data' => [
                [
                    'id' => 1,
                    'name' => 'Arabic',
                    'image' => null,
                    'code' => 'ar',
                    'mobile_app_code' => 'ar',
                    'rtl' => true,
                    'is_default' => true,
                ],
                [
                    'id' => 2,
                    'name' => 'English',
                    'image' => null,
                    'code' => 'en',
                    'mobile_app_code' => 'en',
                    'rtl' => false,
                    'is_default' => false,
                ],
            ],
            'success' => true,
            'status' => 200,
        ]);
    }

    public function currencies(): JsonResponse
    {
        return response()->json([
            'data' => [
                [
                    'id' => 1,
                    'name' => 'Jordanian Dinar',
                    'code' => 'JOD',
                    'symbol' => 'JOD',
                    'exchange_rate' => 1.0,
                    'is_default' => true,
                ],
            ],
            'success' => true,
            'status' => 200,
        ]);
    }
}
