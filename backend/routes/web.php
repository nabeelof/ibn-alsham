<?php

use App\Http\Controllers\AdminAuthController;
use App\Livewire\Admin\Accounting;
use App\Livewire\Admin\AdminManager;
use App\Livewire\Admin\BannerManager;
use App\Livewire\Admin\CategoryManager;
use App\Livewire\Admin\CouponManager;
use App\Livewire\Admin\CustomerManager;
use App\Livewire\Admin\Dashboard;
use App\Livewire\Admin\DeliveryZoneManager;
use App\Livewire\Admin\ExpenseManager;
use App\Livewire\Admin\MenuManager;
use App\Livewire\Admin\NotificationComposer;
use App\Livewire\Admin\OptionGroupManager;
use App\Livewire\Admin\OrderManager;
use App\Livewire\Admin\PaymentMethodManager;
use App\Livewire\Admin\Reports;
use App\Livewire\Admin\RoleManager;
use App\Livewire\Admin\SettingsManager;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return redirect()->route('admin.login');
});

// Admin auth
Route::get('/admin/login', [AdminAuthController::class, 'showLogin'])->name('admin.login');
Route::post('/admin/login', [AdminAuthController::class, 'login'])->name('admin.login.submit');
Route::post('/admin/logout', [AdminAuthController::class, 'logout'])->name('admin.logout');

/*
 * Each screen enforces its own permission in AdminPage::mount(), so the routes
 * only need the auth guard.
 */
Route::middleware(['auth:admin'])->prefix('admin')->name('admin.')->group(function () {
    Route::get('/dashboard', Dashboard::class)->name('dashboard');
    Route::get('/orders', OrderManager::class)->name('orders');

    // Menu
    Route::get('/menu', MenuManager::class)->name('menu');
    Route::get('/categories', CategoryManager::class)->name('categories');
    Route::get('/options', OptionGroupManager::class)->name('options');

    // Marketing
    Route::get('/banners', BannerManager::class)->name('banners');
    Route::get('/coupons', CouponManager::class)->name('coupons');
    Route::get('/notifications', NotificationComposer::class)->name('notifications');

    // Reports & accounting
    Route::get('/reports', Reports::class)->name('reports');
    Route::get('/accounting', Accounting::class)->name('accounting');
    Route::get('/expenses', ExpenseManager::class)->name('expenses');

    // Customers
    Route::get('/customers', CustomerManager::class)->name('customers');

    // Configuration
    Route::get('/settings', SettingsManager::class)->name('settings');
    Route::get('/payments', PaymentMethodManager::class)->name('payments');
    Route::get('/delivery', DeliveryZoneManager::class)->name('delivery');
    Route::get('/admins', AdminManager::class)->name('admins');
    Route::get('/roles', RoleManager::class)->name('roles');
});
