<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('coupons', function (Blueprint $table) {
            $table->id();
            $table->string('code')->unique();
            $table->string('title_ar');
            $table->string('title_en');
            // percent -> discount_value is 0..100, fixed -> discount_value is a JOD amount
            $table->enum('discount_type', ['percent', 'fixed'])->default('percent');
            $table->decimal('discount_value', 10, 3);
            // Caps a percentage coupon, e.g. "20% off, up to 5.000 JOD"
            $table->decimal('max_discount', 10, 3)->nullable();
            $table->decimal('min_order_total', 10, 3)->default(0);
            $table->unsignedInteger('usage_limit')->nullable();      // null = unlimited
            $table->unsignedInteger('usage_limit_per_user')->nullable();
            $table->unsignedInteger('used_count')->default(0);
            $table->timestamp('starts_at')->nullable();
            $table->timestamp('expires_at')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['code', 'is_active']);
        });

        Schema::create('coupon_usages', function (Blueprint $table) {
            $table->id();
            $table->foreignId('coupon_id')->constrained()->cascadeOnDelete();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->foreignId('order_id')->nullable()->constrained()->nullOnDelete();
            $table->decimal('discount_amount', 10, 3);
            $table->timestamps();

            $table->index(['coupon_id', 'user_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('coupon_usages');
        Schema::dropIfExists('coupons');
    }
};
