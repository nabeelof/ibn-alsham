<?php

namespace App\Livewire\Admin;

use App\Models\Order;
use Livewire\Attributes\Layout;
use Livewire\Component;
use Livewire\WithPagination;

#[Layout('components.layouts.admin')]
class OrderManager extends Component
{
    use WithPagination;

    public string $search = '';
    public string $statusFilter = '';
    public ?int $viewingOrderId = null;

    // Confirm modal
    public ?int $confirmingOrderId = null;
    public int $estimatedTime = 40; // minutes default

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function updatingStatusFilter(): void
    {
        $this->resetPage();
    }

    public function updateStatus(int $orderId, string $status): void
    {
        $order = Order::findOrFail($orderId);
        $order->update(['status' => $status]);

        session()->flash('message', "تم تحديث حالة الطلب #{$order->order_number}");
    }

    public function showConfirmModal(int $orderId): void
    {
        $this->confirmingOrderId = $orderId;
        $this->estimatedTime = 40;
    }

    public function confirmOrder(): void
    {
        if (!$this->confirmingOrderId) return;

        $order = Order::findOrFail($this->confirmingOrderId);
        $order->confirm($this->estimatedTime, false);

        $this->confirmingOrderId = null;
        session()->flash('message', "تم تأكيد الطلب #{$order->order_number} بوقت تقديري {$this->estimatedTime} دقيقة");
    }

    public function cancelConfirmModal(): void
    {
        $this->confirmingOrderId = null;
    }

    public function viewOrder(int $orderId): void
    {
        $this->viewingOrderId = $orderId;
    }

    public function closeModal(): void
    {
        $this->viewingOrderId = null;
    }

    public function render()
    {
        $orders = Order::with(['user', 'items.product'])
            ->when($this->search, fn ($q) => $q->where('order_number', 'like', "%{$this->search}%"))
            ->when($this->statusFilter, fn ($q) => $q->where('status', $this->statusFilter))
            ->latest()
            ->paginate(10);

        $viewingOrder = $this->viewingOrderId
            ? Order::with(['user', 'items.product', 'items.options'])->find($this->viewingOrderId)
            : null;

        $confirmingOrder = $this->confirmingOrderId
            ? Order::find($this->confirmingOrderId)
            : null;

        return view('livewire.admin.order-manager', [
            'orders' => $orders,
            'viewingOrder' => $viewingOrder,
            'confirmingOrder' => $confirmingOrder,
        ])->title('إدارة الطلبات');
    }
}
