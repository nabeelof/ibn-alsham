<?php

namespace Database\Seeders;

use App\Models\Coupon;
use Illuminate\Database\Seeder;

class CouponSeeder extends Seeder
{
    public function run(): void
    {
        $coupons = [
            [
                'code' => 'WELCOME10',
                'title_ar' => 'خصم 10% على أول طلب',
                'title_en' => '10% off your first order',
                'discount_type' => 'percent',
                'discount_value' => 10,
                'max_discount' => 3.000,
                'min_order_total' => 5.000,
                'usage_limit' => null,
                'usage_limit_per_user' => 1,
                'is_active' => true,
            ],
            [
                'code' => 'SHAM2',
                'title_ar' => 'دينارين خصم على الطلبات فوق 15 دينار',
                'title_en' => '2 JOD off orders over 15 JOD',
                'discount_type' => 'fixed',
                'discount_value' => 2.000,
                'max_discount' => null,
                'min_order_total' => 15.000,
                'usage_limit' => 500,
                'usage_limit_per_user' => 3,
                'is_active' => true,
            ],
        ];

        foreach ($coupons as $coupon) {
            Coupon::updateOrCreate(['code' => $coupon['code']], $coupon);
        }
    }
}
