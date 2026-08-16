<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('roles', function (Blueprint $table) {
            $table->id();
            $table->string('key')->unique();
            $table->string('name_ar');
            $table->string('name_en');
            $table->string('description_ar')->nullable();
            // System roles ship with the app and cannot be deleted from the UI.
            $table->boolean('is_system')->default(false);
            $table->timestamps();
        });

        Schema::create('role_permissions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('role_id')->constrained()->cascadeOnDelete();
            $table->string('permission');
            $table->timestamps();

            $table->unique(['role_id', 'permission']);
        });

        Schema::table('admins', function (Blueprint $table) {
            $table->foreignId('role_id')->nullable()->after('role')
                  ->constrained('roles')->nullOnDelete();
            $table->string('phone')->nullable()->after('email');
            $table->timestamp('last_login_at')->nullable()->after('is_active');
        });
    }

    public function down(): void
    {
        Schema::table('admins', function (Blueprint $table) {
            $table->dropForeign(['role_id']);
            $table->dropColumn(['role_id', 'phone', 'last_login_at']);
        });

        Schema::dropIfExists('role_permissions');
        Schema::dropIfExists('roles');
    }
};
