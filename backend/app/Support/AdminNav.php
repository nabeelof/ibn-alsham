<?php

namespace App\Support;

use App\Models\Admin;

/**
 * Sidebar definition. Each item declares the permissions that reveal it, so a
 * role never sees a link it cannot open.
 */
class AdminNav
{
    public static function sections(): array
    {
        return [
            [
                'label' => null,
                'items' => [
                    ['route' => 'admin.dashboard', 'label' => 'لوحة التحكّم', 'icon' => 'home', 'can' => [Permissions::DASHBOARD_VIEW]],
                    ['route' => 'admin.orders', 'label' => 'الطلبات', 'icon' => 'receipt', 'can' => [Permissions::ORDERS_VIEW], 'badge' => 'pending_orders'],
                ],
            ],
            [
                'label' => 'قائمة الطعام',
                'items' => [
                    ['route' => 'admin.menu', 'label' => 'المنتجات', 'icon' => 'food', 'can' => [Permissions::PRODUCTS_VIEW]],
                    ['route' => 'admin.categories', 'label' => 'الأقسام', 'icon' => 'grid', 'can' => [Permissions::CATEGORIES_MANAGE]],
                    ['route' => 'admin.options', 'label' => 'الإضافات والخيارات', 'icon' => 'plus-circle', 'can' => [Permissions::OPTIONS_MANAGE]],
                ],
            ],
            [
                'label' => 'التسويق',
                'items' => [
                    ['route' => 'admin.banners', 'label' => 'البانرات', 'icon' => 'image', 'can' => [Permissions::BANNERS_MANAGE]],
                    ['route' => 'admin.coupons', 'label' => 'الكوبونات', 'icon' => 'ticket', 'can' => [Permissions::COUPONS_MANAGE]],
                    ['route' => 'admin.notifications', 'label' => 'الإشعارات', 'icon' => 'bell', 'can' => [Permissions::NOTIFICATIONS_SEND]],
                ],
            ],
            [
                'label' => 'التقارير والمحاسبة',
                'items' => [
                    ['route' => 'admin.reports', 'label' => 'التقارير', 'icon' => 'chart', 'can' => [Permissions::REPORTS_VIEW]],
                    ['route' => 'admin.accounting', 'label' => 'المحاسبة والأرباح', 'icon' => 'calculator', 'can' => [Permissions::ACCOUNTING_VIEW]],
                    ['route' => 'admin.expenses', 'label' => 'المصروفات', 'icon' => 'wallet', 'can' => [Permissions::EXPENSES_MANAGE]],
                ],
            ],
            [
                'label' => 'الزبائن',
                'items' => [
                    ['route' => 'admin.customers', 'label' => 'الزبائن', 'icon' => 'users', 'can' => [Permissions::CUSTOMERS_VIEW]],
                ],
            ],
            [
                'label' => 'الإعدادات',
                'items' => [
                    ['route' => 'admin.settings', 'label' => 'إعدادات التطبيق', 'icon' => 'cog', 'can' => [Permissions::SETTINGS_MANAGE]],
                    ['route' => 'admin.payments', 'label' => 'وسائل الدفع', 'icon' => 'credit-card', 'can' => [Permissions::PAYMENTS_MANAGE]],
                    ['route' => 'admin.delivery', 'label' => 'مناطق التوصيل', 'icon' => 'truck', 'can' => [Permissions::DELIVERY_MANAGE]],
                    ['route' => 'admin.admins', 'label' => 'المشرفون', 'icon' => 'shield', 'can' => [Permissions::ADMINS_MANAGE]],
                    ['route' => 'admin.roles', 'label' => 'الأدوار والصلاحيات', 'icon' => 'key', 'can' => [Permissions::ROLES_MANAGE]],
                ],
            ],
        ];
    }

    /** Sections filtered down to what this admin may actually open. */
    public static function for(?Admin $admin): array
    {
        if (!$admin) {
            return [];
        }

        $visible = [];

        foreach (self::sections() as $section) {
            $items = array_values(array_filter(
                $section['items'],
                fn ($item) => $admin->hasAnyPermission($item['can'])
            ));

            if ($items) {
                $visible[] = ['label' => $section['label'], 'items' => $items];
            }
        }

        return $visible;
    }
}
