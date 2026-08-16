<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('orders', function (Blueprint $table) {
            $table->foreignId('coupon_id')->nullable()->after('notes')
                  ->constrained('coupons')->nullOnDelete();
            $table->string('coupon_code')->nullable()->after('coupon_id');
            $table->decimal('discount_amount', 10, 3)->default(0)->after('coupon_code');
            $table->enum('payment_status', ['unpaid', 'pending', 'paid', 'failed', 'refunded'])
                  ->default('unpaid')->after('payment_method');
            $table->string('payment_reference')->nullable()->after('payment_status');
        });

        // Widen payment_method from the cash-only enum to the Jordan method keys.
        // Done as raw SQL because Doctrine DBAL is not installed for enum changes.
        $driver = Schema::getConnection()->getDriverName();
        if ($driver === 'mysql') {
            Schema::getConnection()->statement(
                "ALTER TABLE orders MODIFY payment_method
                 ENUM('cash', 'cod', 'cliq', 'card', 'wallet') NOT NULL DEFAULT 'cash'"
            );
        }
        // SQLite stores enums as text, so nothing to widen there.
    }

    public function down(): void
    {
        Schema::table('orders', function (Blueprint $table) {
            $table->dropForeign(['coupon_id']);
            $table->dropColumn([
                'coupon_id', 'coupon_code', 'discount_amount', 'payment_status', 'payment_reference',
            ]);
        });

        if (Schema::getConnection()->getDriverName() === 'mysql') {
            Schema::getConnection()->statement(
                "ALTER TABLE orders MODIFY payment_method ENUM('cash') NOT NULL DEFAULT 'cash'"
            );
        }
    }
};
