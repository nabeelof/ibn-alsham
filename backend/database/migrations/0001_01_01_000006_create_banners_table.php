<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('banners', function (Blueprint $table) {
            $table->id();
            $table->string('image');
            $table->string('title_ar')->nullable();
            $table->string('title_en')->nullable();
            $table->enum('link_type', ['none', 'category', 'product', 'url'])->default('none');
            $table->string('link_value')->nullable(); // category_id, product_id, or external url
            $table->integer('sort_order')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('banners');
    }
};
