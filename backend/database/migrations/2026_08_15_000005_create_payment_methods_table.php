<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('payment_methods', function (Blueprint $table) {
            $table->id();
            $table->string('key')->unique();          // cash, cliq, card, wallet
            $table->string('name_ar');
            $table->string('name_en');
            $table->string('description_ar')->nullable();
            $table->string('description_en')->nullable();
            $table->string('icon')->nullable();
            // Instructions shown for manual transfer methods (CliQ alias, etc.)
            $table->string('account_alias')->nullable();
            $table->boolean('requires_reference')->default(false);
            $table->boolean('is_online')->default(false);
            $table->boolean('available_for_pickup')->default(true);
            $table->boolean('available_for_delivery')->default(true);
            $table->boolean('is_active')->default(true);
            $table->integer('sort_order')->default(0);
            $table->timestamps();
        });

        // Jordan payment methods.
        $now = now();
        DB::table('payment_methods')->insert([
            [
                'key' => 'cash',
                'name_ar' => 'الدفع عند الاستلام',
                'name_en' => 'Cash on Delivery',
                'description_ar' => 'ادفع نقداً عند استلام طلبك',
                'description_en' => 'Pay in cash when your order arrives',
                'icon' => null,
                'account_alias' => null,
                'requires_reference' => false,
                'is_online' => false,
                'available_for_pickup' => true,
                'available_for_delivery' => true,
                'is_active' => true,
                'sort_order' => 1,
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'key' => 'cliq',
                'name_ar' => 'كليك (CliQ)',
                'name_en' => 'CliQ',
                'description_ar' => 'حوّل المبلغ عبر كليك من تطبيق بنكك ثم أرفق رقم الحوالة',
                'description_en' => 'Transfer via CliQ from your bank app, then enter the reference',
                'icon' => null,
                'account_alias' => 'IBNALSHAM',
                'requires_reference' => true,
                'is_online' => false,
                'available_for_pickup' => true,
                'available_for_delivery' => true,
                'is_active' => true,
                'sort_order' => 2,
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'key' => 'card',
                'name_ar' => 'بطاقة ائتمان / مدى',
                'name_en' => 'Credit / Debit Card',
                'description_ar' => 'ادفع بأمان ببطاقة فيزا أو ماستركارد',
                'description_en' => 'Pay securely with Visa or Mastercard',
                'icon' => null,
                'account_alias' => null,
                'requires_reference' => false,
                'is_online' => true,
                'available_for_pickup' => true,
                'available_for_delivery' => true,
                'is_active' => false, // enable once the payment gateway is contracted
                'sort_order' => 3,
                'created_at' => $now,
                'updated_at' => $now,
            ],
            [
                'key' => 'wallet',
                'name_ar' => 'محفظة ابن الشام',
                'name_en' => 'Ibn Alsham Wallet',
                'description_ar' => 'ادفع من رصيد محفظتك',
                'description_en' => 'Pay from your wallet balance',
                'icon' => null,
                'account_alias' => null,
                'requires_reference' => false,
                'is_online' => true,
                'available_for_pickup' => true,
                'available_for_delivery' => true,
                'is_active' => false,
                'sort_order' => 4,
                'created_at' => $now,
                'updated_at' => $now,
            ],
        ]);
    }

    public function down(): void
    {
        Schema::dropIfExists('payment_methods');
    }
};
