<?php

namespace Database\Seeders;

use App\Models\Admin;
use App\Models\ExpenseCategory;
use App\Models\Role;
use App\Support\Permissions;
use Illuminate\Database\Seeder;

class AdminAccessSeeder extends Seeder
{
    public function run(): void
    {
        // System roles with their permission sets.
        foreach (Permissions::presets() as $key => $preset) {
            $role = Role::updateOrCreate(
                ['key' => $key],
                [
                    'name_ar' => $preset['name_ar'],
                    'name_en' => $preset['name_en'],
                    'is_system' => true,
                ]
            );

            $role->load('permissions');
            $role->syncPermissions($preset['permissions']);
        }

        // Attach existing admins to the role row matching their legacy string.
        Admin::whereNull('role_id')->get()->each(function (Admin $admin) {
            $role = Role::where('key', $admin->role)->first();
            if ($role) {
                $admin->update(['role_id' => $role->id]);
            }
        });

        // Starter expense categories for the accounting module.
        $categories = [
            ['name_ar' => 'مشتريات ومواد أولية', 'name_en' => 'Purchases & Raw Materials', 'color' => '#DC2626'],
            ['name_ar' => 'رواتب وأجور', 'name_en' => 'Salaries & Wages', 'color' => '#2563EB'],
            ['name_ar' => 'إيجار', 'name_en' => 'Rent', 'color' => '#7C3AED'],
            ['name_ar' => 'كهرباء وماء', 'name_en' => 'Utilities', 'color' => '#059669'],
            ['name_ar' => 'توصيل ومواصلات', 'name_en' => 'Delivery & Transport', 'color' => '#EA580C'],
            ['name_ar' => 'صيانة', 'name_en' => 'Maintenance', 'color' => '#0891B2'],
            ['name_ar' => 'تسويق وإعلان', 'name_en' => 'Marketing', 'color' => '#DB2777'],
            ['name_ar' => 'مصروفات أخرى', 'name_en' => 'Other', 'color' => '#6B7280'],
        ];

        foreach ($categories as $category) {
            ExpenseCategory::firstOrCreate(['name_en' => $category['name_en']], $category);
        }
    }
}
