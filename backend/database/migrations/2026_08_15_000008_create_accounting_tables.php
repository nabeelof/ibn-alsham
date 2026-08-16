<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('expense_categories', function (Blueprint $table) {
            $table->id();
            $table->string('name_ar');
            $table->string('name_en');
            $table->string('color', 16)->default('#6B7280');
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });

        Schema::create('expenses', function (Blueprint $table) {
            $table->id();
            $table->foreignId('expense_category_id')->nullable()
                  ->constrained()->nullOnDelete();
            $table->foreignId('admin_id')->nullable()->constrained()->nullOnDelete();
            $table->string('title');
            $table->decimal('amount', 10, 3);
            $table->date('spent_on');
            $table->enum('payment_method', ['cash', 'cliq', 'card', 'bank'])->default('cash');
            $table->string('reference')->nullable();  // invoice / transfer number
            $table->text('notes')->nullable();
            $table->boolean('is_recurring')->default(false);
            $table->timestamps();

            $table->index('spent_on');
        });

        // Cost price lets the P&L compute gross margin per order line.
        Schema::table('products', function (Blueprint $table) {
            $table->decimal('cost_price', 10, 3)->nullable()->after('price');
        });

        // Snapshot the cost at order time so historical margins stay accurate
        // when the purchase price later changes.
        Schema::table('order_items', function (Blueprint $table) {
            $table->decimal('unit_cost', 10, 3)->nullable()->after('unit_price');
        });
    }

    public function down(): void
    {
        Schema::table('order_items', fn (Blueprint $table) => $table->dropColumn('unit_cost'));
        Schema::table('products', fn (Blueprint $table) => $table->dropColumn('cost_price'));
        Schema::dropIfExists('expenses');
        Schema::dropIfExists('expense_categories');
    }
};
