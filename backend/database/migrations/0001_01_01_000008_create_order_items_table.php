<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('order_items', function (Blueprint $table) {
            $table->id();
            $table->foreignId('order_id')->constrained()->cascadeOnDelete();
            $table->foreignId('product_id')->constrained();
            $table->string('product_name_ar'); // snapshot at order time
            $table->string('product_name_en'); // snapshot at order time
            $table->unsignedInteger('quantity');
            $table->decimal('unit_price', 10, 3);
            $table->decimal('discount_price', 10, 3)->nullable();
            $table->decimal('options_total', 10, 3)->default(0);
            $table->decimal('total', 10, 3); // (discount_price ?? unit_price) * quantity + options_total
            $table->timestamps();
        });

        Schema::create('order_item_options', function (Blueprint $table) {
            $table->id();
            $table->foreignId('order_item_id')->constrained()->cascadeOnDelete();
            $table->foreignId('product_option_id')->constrained();
            $table->string('option_name_ar'); // snapshot
            $table->string('option_name_en'); // snapshot
            $table->decimal('price', 10, 3);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('order_item_options');
        Schema::dropIfExists('order_items');
    }
};
