<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('product_options', function (Blueprint $table) {
            $table->foreignId('option_group_id')->nullable()->after('product_id')
                  ->constrained('product_option_groups')->cascadeOnDelete();
            $table->boolean('default_selected')->default(false)->after('is_required');
            $table->boolean('is_user_changeable')->default(true)->after('default_selected');
        });

        // Migrate existing options: create a default group per product
        $productIds = DB::table('product_options')
            ->whereNull('option_group_id')
            ->distinct()
            ->pluck('product_id');

        foreach ($productIds as $productId) {
            $groupId = DB::table('product_option_groups')->insertGetId([
                'product_id' => $productId,
                'name_ar' => 'إضافات',
                'name_en' => 'Extras',
                'type' => 'checkbox',
                'is_required' => false,
                'is_skippable' => true,
                'min_selections' => 0,
                'max_selections' => null,
                'sort_order' => 0,
                'is_active' => true,
                'created_at' => now(),
                'updated_at' => now(),
            ]);

            DB::table('product_options')
                ->where('product_id', $productId)
                ->whereNull('option_group_id')
                ->update(['option_group_id' => $groupId]);
        }
    }

    public function down(): void
    {
        Schema::table('product_options', function (Blueprint $table) {
            $table->dropForeign(['option_group_id']);
            $table->dropColumn(['option_group_id', 'default_selected', 'is_user_changeable']);
        });
    }
};
