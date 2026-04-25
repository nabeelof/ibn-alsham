<?php

namespace App\Livewire\Admin;

use App\Models\Order;
use App\Models\Product;
use App\Models\User;
use Livewire\Attributes\Layout;
use Livewire\Component;

#[Layout('components.layouts.admin')]
class Dashboard extends Component
{
    public function render()
    {
        return view('livewire.admin.dashboard', [
            'totalUsers' => User::count(),
            'totalOrders' => Order::count(),
            'todayOrders' => Order::whereDate('created_at', today())->count(),
            'todayRevenue' => Order::whereDate('created_at', today())->whereIn('status', ['delivered', 'completed'])->sum('total'),
            'recentOrders' => Order::with('user')->latest()->take(5)->get(),
        ])->title('لوحة التحكم الرئيسية');
    }
}
