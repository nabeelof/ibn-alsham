<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        // Map old roles to new roles
        DB::table('admins')->where('role', 'admin')->update(['role' => 'content_manager']);
        DB::table('admins')->where('role', 'staff')->update(['role' => 'order_manager']);

        // Alter enum
        DB::statement("ALTER TABLE admins MODIFY COLUMN role ENUM('super_admin', 'content_manager', 'order_manager') DEFAULT 'order_manager'");
    }

    public function down(): void
    {
        DB::table('admins')->where('role', 'content_manager')->update(['role' => 'admin']);
        DB::table('admins')->where('role', 'order_manager')->update(['role' => 'staff']);

        DB::statement("ALTER TABLE admins MODIFY COLUMN role ENUM('super_admin', 'admin', 'staff') DEFAULT 'admin'");
    }
};
