<?php

namespace App\Jobs;

use App\Models\Order;
use App\Models\Notification;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;

class CheckDeliveryStatusJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public function __construct(
        private int $orderId,
    ) {}

    public function handle(): void
    {
        $order = Order::find($this->orderId);

        if (!$order) return;

        // Only check if order is still in an active state
        if (!in_array($order->status, ['confirmed', 'preparing', 'ready', 'out_for_delivery'])) return;

        // Already confirmed delivery
        if ($order->delivery_confirmed_at) return;

        // Create a notification asking user to confirm delivery
        Notification::create([
            'user_id' => $order->user_id,
            'type' => 'delivery_check',
            'title_ar' => 'هل وصل طلبك؟',
            'title_en' => 'Has your order arrived?',
            'body_ar' => "طلبك رقم {$order->order_number} - يرجى تأكيد استلام الطلب",
            'body_en' => "Order #{$order->order_number} - Please confirm you received your order",
            'data' => json_encode(['order_id' => $order->id, 'action' => 'delivery_check']),
        ]);

        // TODO: Later send FCM push notification
    }
}
