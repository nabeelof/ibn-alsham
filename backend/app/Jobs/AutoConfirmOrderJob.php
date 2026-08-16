<?php

namespace App\Jobs;

use App\Models\Order;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;

class AutoConfirmOrderJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public function __construct(
        private int $orderId,
    ) {}

    public function handle(): void
    {
        $order = Order::find($this->orderId);

        if (!$order) return;

        // Only auto-confirm if still pending
        if ($order->status !== 'pending') return;

        $order->confirm(
            estimatedMinutes: 40,
            autoConfirmed: true,
        );

        // Dispatch delivery check job 40 minutes from now
        CheckDeliveryStatusJob::dispatch($this->orderId)->delay(now()->addMinutes(40));
    }
}
