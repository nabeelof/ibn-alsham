<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('orders', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('order_number')->unique(); // e.g. IBN-20260410-0001
            $table->enum('type', ['pickup', 'delivery']);
            $table->enum('status', [
                'pending',       // بانتظار التأكيد
                'confirmed',     // مؤكد
                'preparing',     // قيد التحضير
                'ready',         // جاهز
                'out_for_delivery', // في الطريق (delivery only)
                'delivered',     // تم التوصيل
                'completed',     // مكتمل
                'cancelled',     // ملغي
            ])->default('pending');
            $table->decimal('subtotal', 10, 3);
            $table->decimal('delivery_fee', 10, 3)->default(0);
            $table->decimal('total', 10, 3);
            $table->text('address')->nullable(); // for delivery
            $table->decimal('lat', 10, 7)->nullable();
            $table->decimal('lng', 10, 7)->nullable();
            $table->decimal('distance_km', 8, 2)->nullable();
            $table->text('notes')->nullable();
            $table->enum('payment_method', ['cash'])->default('cash'); // cash only for now
            $table->timestamp('confirmed_at')->nullable();
            $table->unsignedInteger('estimated_time')->nullable(); // minutes
            $table->timestamp('cancellation_deadline')->nullable(); // 3 min after confirmation
            $table->string('cancel_reason')->nullable();
            $table->timestamps();

            $table->index(['user_id', 'status']);
            $table->index('order_number');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('orders');
    }
};
