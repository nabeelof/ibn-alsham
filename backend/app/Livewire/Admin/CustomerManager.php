<?php

namespace App\Livewire\Admin;

use App\Models\Order;
use App\Models\User;
use App\Services\ReportingService;
use App\Support\Permissions;
use Livewire\WithPagination;

class CustomerManager extends AdminPage
{
    use WithPagination;

    protected string $permission = Permissions::CUSTOMERS_VIEW;

    public string $search = '';
    public string $sort = 'recent';
    public ?int $viewingId = null;

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function view(int $id): void
    {
        $this->viewingId = $id;
    }

    public function closeModal(): void
    {
        $this->viewingId = null;
    }

    public function render()
    {
        $customers = User::query()
            ->withCount(['orders as orders_count'])
            ->withSum(
                ['orders as spent' => fn ($q) => $q->whereIn('status', ReportingService::REVENUE_STATUSES)],
                'total'
            )
            ->when($this->search, fn ($q) => $q->where(function ($sub) {
                $sub->where('name', 'like', "%{$this->search}%")
                    ->orWhere('phone', 'like', "%{$this->search}%");
            }))
            ->when($this->sort === 'spend', fn ($q) => $q->orderByDesc('spent'))
            ->when($this->sort === 'orders', fn ($q) => $q->orderByDesc('orders_count'))
            ->when($this->sort === 'recent', fn ($q) => $q->latest())
            ->paginate(15);

        $viewing = $this->viewingId ? User::find($this->viewingId) : null;
        $viewingOrders = $viewing
            ? Order::where('user_id', $viewing->id)->latest()->take(10)->get()
            : collect();

        return view('livewire.admin.customer-manager', [
            'customers' => $customers,
            'viewing' => $viewing,
            'viewingOrders' => $viewingOrders,
            'totalCustomers' => User::count(),
            'verifiedCustomers' => User::where('is_verified', true)->count(),
            'withPushToken' => User::whereNotNull('fcm_token')->count(),
        ])->title('الزبائن');
    }
}
