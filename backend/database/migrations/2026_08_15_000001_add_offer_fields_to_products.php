<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('products', function (Blueprint $table) {
            // Offer type: percentage discount (%) or a flat JOD amount off.
            $table->enum('discount_type', ['none', 'percent', 'fixed'])
                  ->default('none')->after('discounted_price');
            // percent -> 0..100, fixed -> JOD amount (3 decimals)
            $table->decimal('discount_value', 10, 3)->nullable()->after('discount_type');
            $table->timestamp('offer_starts_at')->nullable()->after('discount_value');
            $table->timestamp('offer_ends_at')->nullable()->after('offer_starts_at');
        });

        // Existing rows already carry a pre-computed discounted_price. Back-fill the
        // offer type as a fixed amount off so the two representations agree.
        DB::table('products')
            ->whereNotNull('discounted_price')
            ->where('discounted_price', '>', 0)
            ->update([
                'discount_type' => 'fixed',
                'discount_value' => DB::raw('price - discounted_price'),
            ]);
    }

    public function down(): void
    {
        Schema::table('products', function (Blueprint $table) {
            $table->dropColumn(['discount_type', 'discount_value', 'offer_starts_at', 'offer_ends_at']);
        });
    }
};
