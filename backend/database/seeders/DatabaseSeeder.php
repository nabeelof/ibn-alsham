<?php

namespace Database\Seeders;

use App\Models\Admin;
use App\Models\Banner;
use App\Models\Category;
use App\Models\Product;
use App\Models\ProductOption;
use App\Models\Setting;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // 1. Create Admin
        Admin::create([
            'name' => 'Super Admin',
            'email' => 'admin@ibnalsham.com',
            'password' => Hash::make('password123'), // Use secure password in production
            'role' => 'super_admin',
        ]);

        // 2. Create Settings
        $settings = [
            ['key' => 'about_us', 'value_ar' => 'مطعم ابن الشام، حيث الأصالة الدمشقية في عمان.', 'value_en' => 'Ibn Alsham Restaurant, where Damascene authenticity lives in Amman.', 'group' => 'general'],
            ['key' => 'contact_phone', 'value_ar' => '+962700000000', 'value_en' => '+962700000000', 'group' => 'contact'],
            ['key' => 'whatsapp', 'value_ar' => '+962700000000', 'value_en' => '+962700000000', 'group' => 'contact'],
            ['key' => 'facebook_url', 'value_ar' => 'https://facebook.com/ibnalsham', 'value_en' => 'https://facebook.com/ibnalsham', 'group' => 'contact'],
            ['key' => 'privacy_policy', 'value_ar' => 'نحن نحترم خصوصيتك...', 'value_en' => 'We respect your privacy...', 'group' => 'legal'],
            ['key' => 'terms_conditions', 'value_ar' => 'شروط استخدام التطبيق...', 'value_en' => 'Application terms of use...', 'group' => 'legal'],
        ];
        foreach ($settings as $setting) Setting::create($setting);

        // 3. Create Categories
        $catShawarma = Category::create(['name_ar' => 'شاورما', 'name_en' => 'Shawarma', 'sort_order' => 1]);
        $catGrills = Category::create(['name_ar' => 'مشاوي', 'name_en' => 'Grills', 'sort_order' => 2]);
        $catAppetizers = Category::create(['name_ar' => 'مقبلات', 'name_en' => 'Appetizers', 'sort_order' => 3]);
        $catDrinks = Category::create(['name_ar' => 'مشروبات', 'name_en' => 'Drinks', 'sort_order' => 4]);

        // 4. Create Products
        $p1 = Product::create([
            'category_id' => $catShawarma->id,
            'name_ar' => 'وجبة شاورما دجاج سوبر',
            'name_en' => 'Super Chicken Shawarma Meal',
            'description_ar' => 'وجبة شاورما دجاج مقطعة مع بطاطا ومثومة ومخلل.',
            'description_en' => 'Sliced chicken shawarma meal with fries, garlic dip, and pickles.',
            'price' => 3.500,
            'is_featured' => true,
            'order_count' => 150,
        ]);
        
        // Add options to p1
        ProductOption::create(['product_id' => $p1->id, 'name_ar' => 'بدون مخلل', 'name_en' => 'No Pickles', 'price' => 0]);
        ProductOption::create(['product_id' => $p1->id, 'name_ar' => 'إضافة جبنة', 'name_en' => 'Add Cheese', 'price' => 0.500]);
        ProductOption::create(['product_id' => $p1->id, 'name_ar' => 'مثومة إضافية', 'name_en' => 'Extra Garlic Dip', 'price' => 0.250]);

        $p2 = Product::create([
            'category_id' => $catGrills->id,
            'name_ar' => 'مشاوي مشكل (نصف كيلو)',
            'name_en' => 'Mixed Grills (Half Kilo)',
            'description_ar' => 'تشكيلة من الكباب، الشقف، والشيش طاووق مع الخضار المشوية.',
            'description_en' => 'Assortment of kabab, shaqaf, and shish tawook with grilled vegetables.',
            'price' => 8.000,
            'discounted_price' => 7.000, // Has discount
            'is_featured' => true,
        ]);

        $p3 = Product::create([
            'category_id' => $catAppetizers->id,
            'name_ar' => 'حمص شامي',
            'name_en' => 'Damascene Hummus',
            'description_ar' => 'حمص بطحينة وزيت الزيتون البكر.',
            'description_en' => 'Hummus with tahini and virgin olive oil.',
            'price' => 1.500,
            'order_count' => 200, // Most ordered
        ]);

        $p4 = Product::create([
            'category_id' => $catShawarma->id,
            'name_ar' => 'ساندويش شاورما كبير',
            'name_en' => 'Large Shawarma Sandwich',
            'price' => 2.000,
            'is_featured' => true,
        ]);

        $p5 = Product::create([
            'category_id' => $catGrills->id,
            'name_ar' => 'عرايس لحمة (4 قطع)',
            'name_en' => 'Meat Arayes (4 pcs)',
            'price' => 4.500,
        ]);

        $p6 = Product::create([
            'category_id' => $catDrinks->id,
            'name_ar' => 'بيبسي',
            'name_en' => 'Pepsi',
            'price' => 0.500,
        ]);

        // 5. Create Banners
        Banner::create([
            'image' => 'banners/dummy_banner1.jpg',
            'title_ar' => 'عرض المشاوي العائلي',
            'title_en' => 'Family Grills Offer',
            'link_type' => 'product',
            'link_value' => $p2->id,
            'sort_order' => 1,
        ]);
        
        Banner::create([
            'image' => 'banners/dummy_banner2.jpg',
            'title_ar' => 'أفضل أنواع الشاورما',
            'title_en' => 'Best Shawarma in Town',
            'link_type' => 'category',
            'link_value' => $catShawarma->id,
            'sort_order' => 2,
        ]);

        // 6. Coupons
        $this->call(CouponSeeder::class);
    }
}
