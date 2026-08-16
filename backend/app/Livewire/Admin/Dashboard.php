<?php

namespace App\Livewire\Admin;

use App\Models\Order;
use App\Models\Product;
use App\Models\User;
use App\Services\ReportingService;
use App\Support\Permissions;
use Livewire\Attributes\Url;

class Dashboard extends AdminPage
{
    protected string $permission = Permissions::DASHBOARD_VIEW;

    #[Url]
    public string $range = 'last_7';

    public function render()
    {
        $reports = app(ReportingService::class);
        [$from, $to] = ReportingService::resolveRange($this->range);

        $summary = $reports->summary($from, $to);
        $daily = $reports->dailyRevenue($from, $to);
        $today = $reports->summary(now()->startOfDay(), now()->endOfDay());

        return view('livewire.admin.dashboard', [
            'summary' => $summary,
            'today' => $today,
            'daily' => $daily,
            'topProducts' => $reports->topProducts($from, $to, 5),
            'paymentBreakdown' => $reports->paymentBreakdown($from, $to),
            'typeSplit' => $reports->orderTypeSplit($from, $to),
            'ranges' => ReportingService::ranges(),
            'pendingOrders' => Order::where('status', 'pending')->count(),
            'activeOrders' => Order::current()->count(),
            'totalCustomers' => User::count(),
            'outOfStock' => Product::where('is_available', false)->count(),
            'liveQueue' => Order::with('user')->current()->latest()->take(8)->get(),
            'canViewAccounting' => $this->can(Permissions::ACCOUNTING_VIEW),
        ])->title('لوحة التحكّم');
    }
}
