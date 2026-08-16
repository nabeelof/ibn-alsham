<?php

namespace App\Support;

/**
 * The permission catalogue for the admin panel.
 *
 * Permissions are plain strings so roles stay editable from the UI without a
 * migration every time a screen is added. Groups drive the layout of the role
 * builder, and the presets seed the system roles.
 */
class Permissions
{
    // Dashboard
    public const DASHBOARD_VIEW = 'dashboard.view';

    // Orders
    public const ORDERS_VIEW = 'orders.view';
    public const ORDERS_UPDATE = 'orders.update';
    public const ORDERS_CANCEL = 'orders.cancel';

    // Catalogue
    public const PRODUCTS_VIEW = 'products.view';
    public const PRODUCTS_MANAGE = 'products.manage';
    public const CATEGORIES_MANAGE = 'categories.manage';
    public const OPTIONS_MANAGE = 'options.manage';

    // Marketing
    public const BANNERS_MANAGE = 'banners.manage';
    public const COUPONS_MANAGE = 'coupons.manage';
    public const NOTIFICATIONS_SEND = 'notifications.send';

    // Customers
    public const CUSTOMERS_VIEW = 'customers.view';
    public const CUSTOMERS_MANAGE = 'customers.manage';

    // Reports & accounting
    public const REPORTS_VIEW = 'reports.view';
    public const REPORTS_EXPORT = 'reports.export';
    public const ACCOUNTING_VIEW = 'accounting.view';
    public const EXPENSES_MANAGE = 'expenses.manage';

    // Configuration
    public const SETTINGS_MANAGE = 'settings.manage';
    public const PAYMENTS_MANAGE = 'payments.manage';
    public const DELIVERY_MANAGE = 'delivery.manage';

    // Access control
    public const ADMINS_MANAGE = 'admins.manage';
    public const ROLES_MANAGE = 'roles.manage';

    /**
     * Permissions grouped for the role builder UI: group label => [permission => label].
     */
    public static function groups(): array
    {
        return [
            'لوحة التحكّم' => [
                self::DASHBOARD_VIEW => 'عرض لوحة التحكّم',
            ],
            'الطلبات' => [
                self::ORDERS_VIEW => 'عرض الطلبات',
                self::ORDERS_UPDATE => 'تحديث حالة الطلب',
                self::ORDERS_CANCEL => 'إلغاء الطلبات',
            ],
            'قائمة الطعام' => [
                self::PRODUCTS_VIEW => 'عرض المنتجات',
                self::PRODUCTS_MANAGE => 'إضافة وتعديل المنتجات',
                self::CATEGORIES_MANAGE => 'إدارة الأقسام',
                self::OPTIONS_MANAGE => 'إدارة الإضافات والخيارات',
            ],
            'التسويق' => [
                self::BANNERS_MANAGE => 'إدارة البانرات',
                self::COUPONS_MANAGE => 'إدارة الكوبونات',
                self::NOTIFICATIONS_SEND => 'إرسال الإشعارات',
            ],
            'الزبائن' => [
                self::CUSTOMERS_VIEW => 'عرض الزبائن',
                self::CUSTOMERS_MANAGE => 'إدارة الزبائن',
            ],
            'التقارير والمحاسبة' => [
                self::REPORTS_VIEW => 'عرض التقارير',
                self::REPORTS_EXPORT => 'تصدير التقارير',
                self::ACCOUNTING_VIEW => 'عرض المحاسبة والأرباح',
                self::EXPENSES_MANAGE => 'إدارة المصروفات',
            ],
            'الإعدادات' => [
                self::SETTINGS_MANAGE => 'إعدادات التطبيق',
                self::PAYMENTS_MANAGE => 'وسائل الدفع',
                self::DELIVERY_MANAGE => 'مناطق ورسوم التوصيل',
            ],
            'الصلاحيات' => [
                self::ADMINS_MANAGE => 'إدارة المشرفين',
                self::ROLES_MANAGE => 'إدارة الأدوار',
            ],
        ];
    }

    /** Every permission key. */
    public static function all(): array
    {
        return array_merge(...array_map('array_keys', array_values(self::groups())));
    }

    public static function label(string $permission): string
    {
        foreach (self::groups() as $items) {
            if (isset($items[$permission])) {
                return $items[$permission];
            }
        }

        return $permission;
    }

    /**
     * System roles seeded on install: key => [name_ar, name_en, permissions].
     * `super_admin` is special-cased everywhere and implicitly holds everything.
     */
    public static function presets(): array
    {
        return [
            'super_admin' => [
                'name_ar' => 'مدير عام',
                'name_en' => 'Super Admin',
                'permissions' => self::all(),
            ],
            'manager' => [
                'name_ar' => 'مدير المطعم',
                'name_en' => 'Restaurant Manager',
                'permissions' => [
                    self::DASHBOARD_VIEW,
                    self::ORDERS_VIEW, self::ORDERS_UPDATE, self::ORDERS_CANCEL,
                    self::PRODUCTS_VIEW, self::PRODUCTS_MANAGE,
                    self::CATEGORIES_MANAGE, self::OPTIONS_MANAGE,
                    self::BANNERS_MANAGE, self::COUPONS_MANAGE, self::NOTIFICATIONS_SEND,
                    self::CUSTOMERS_VIEW,
                    self::REPORTS_VIEW, self::REPORTS_EXPORT,
                    self::ACCOUNTING_VIEW, self::EXPENSES_MANAGE,
                    self::DELIVERY_MANAGE,
                ],
            ],
            'accountant' => [
                'name_ar' => 'محاسب',
                'name_en' => 'Accountant',
                'permissions' => [
                    self::DASHBOARD_VIEW,
                    self::ORDERS_VIEW,
                    self::REPORTS_VIEW, self::REPORTS_EXPORT,
                    self::ACCOUNTING_VIEW, self::EXPENSES_MANAGE,
                ],
            ],
            'order_manager' => [
                'name_ar' => 'مسؤول الطلبات',
                'name_en' => 'Order Manager',
                'permissions' => [
                    self::DASHBOARD_VIEW,
                    self::ORDERS_VIEW, self::ORDERS_UPDATE, self::ORDERS_CANCEL,
                    self::CUSTOMERS_VIEW,
                ],
            ],
            'content_manager' => [
                'name_ar' => 'مسؤول المحتوى',
                'name_en' => 'Content Manager',
                'permissions' => [
                    self::DASHBOARD_VIEW,
                    self::PRODUCTS_VIEW, self::PRODUCTS_MANAGE,
                    self::CATEGORIES_MANAGE, self::OPTIONS_MANAGE,
                    self::BANNERS_MANAGE, self::COUPONS_MANAGE,
                ],
            ],
        ];
    }
}
