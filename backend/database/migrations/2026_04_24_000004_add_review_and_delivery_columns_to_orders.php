<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('orders', function (Blueprint $table) {
            $table->unsignedTinyInteger('rating')->nullable()->after('cancel_reason'); // 1-5
            $table->text('review_note')->nullable()->after('rating');
            $table->timestamp('delivery_confirmed_at')->nullable()->after('review_note');
            $table->boolean('auto_confirmed')->default(false)->after('delivery_confirmed_at');
        });
    }

    public function down(): void
    {
        Schema::table('orders', function (Blueprint $table) {
            $table->dropColumn(['rating', 'review_note', 'delivery_confirmed_at', 'auto_confirmed']);
        });
    }
};
