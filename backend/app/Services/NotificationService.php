<?php

namespace App\Services;

use App\Models\Notification;
use App\Models\User;
use Illuminate\Support\Facades\Log;

class NotificationService
{
    /**
     * Send notification to a specific user.
     */
    public function sendToUser(
        User $user,
        string $titleAr,
        string $titleEn,
        string $bodyAr,
        string $bodyEn,
        string $type = 'general',
        ?array $data = null,
    ): Notification {
        $notification = Notification::create([
            'user_id' => $user->id,
            'title_ar' => $titleAr,
            'title_en' => $titleEn,
            'body_ar' => $bodyAr,
            'body_en' => $bodyEn,
            'type' => $type,
            'data' => $data,
        ]);

        // Send push notification via FCM
        if ($user->fcm_token) {
            $this->sendPushNotification(
                $user->fcm_token,
                $user->locale === 'ar' ? $titleAr : $titleEn,
                $user->locale === 'ar' ? $bodyAr : $bodyEn,
                $data
            );
        }

        return $notification;
    }

    /**
     * Send notification to all users.
     */
    public function sendToAll(
        string $titleAr,
        string $titleEn,
        string $bodyAr,
        string $bodyEn,
        string $type = 'general',
        ?array $data = null,
    ): void {
        User::active()->verified()->chunk(100, function ($users) use ($titleAr, $titleEn, $bodyAr, $bodyEn, $type, $data) {
            foreach ($users as $user) {
                Notification::create([
                    'user_id' => $user->id,
                    'title_ar' => $titleAr,
                    'title_en' => $titleEn,
                    'body_ar' => $bodyAr,
                    'body_en' => $bodyEn,
                    'type' => $type,
                    'data' => $data,
                ]);
            }

            // Batch FCM tokens
            $tokens = $users->pluck('fcm_token')->filter()->values()->toArray();
            if (!empty($tokens)) {
                $this->sendPushToMultiple($tokens, $titleAr, $titleEn, $bodyAr, $bodyEn, $data);
            }
        });
    }

    /**
     * Send order status notification.
     */
    public function notifyOrderStatus(
        User $user,
        string $orderNumber,
        string $status,
        ?int $estimatedTime = null,
    ): void {
        $titles = $this->getOrderStatusTitle($status);
        $bodies = $this->getOrderStatusBody($status, $orderNumber, $estimatedTime);

        $this->sendToUser(
            $user,
            $titles['ar'],
            $titles['en'],
            $bodies['ar'],
            $bodies['en'],
            'order',
            ['order_number' => $orderNumber, 'status' => $status]
        );
    }

    private function getOrderStatusTitle(string $status): array
    {
        return match ($status) {
            'confirmed' => ['ar' => 'تم تأكيد طلبك', 'en' => 'Order Confirmed'],
            'preparing' => ['ar' => 'طلبك قيد التحضير', 'en' => 'Order Being Prepared'],
            'ready' => ['ar' => 'طلبك جاهز! 🎉', 'en' => 'Order Ready! 🎉'],
            'out_for_delivery' => ['ar' => 'طلبك في الطريق', 'en' => 'Order On Its Way'],
            'delivered' => ['ar' => 'تم التوصيل', 'en' => 'Order Delivered'],
            'cancelled' => ['ar' => 'تم إلغاء الطلب', 'en' => 'Order Cancelled'],
            default => ['ar' => 'تحديث الطلب', 'en' => 'Order Update'],
        };
    }

    private function getOrderStatusBody(string $status, string $orderNumber, ?int $estimatedTime): array
    {
        $timeText = $estimatedTime ? " - الوقت المتوقع: {$estimatedTime} دقيقة" : '';
        $timeTextEn = $estimatedTime ? " - Estimated time: {$estimatedTime} min" : '';

        return match ($status) {
            'confirmed' => [
                'ar' => "تم تأكيد طلبك رقم {$orderNumber}{$timeText}",
                'en' => "Your order {$orderNumber} has been confirmed{$timeTextEn}",
            ],
            'preparing' => [
                'ar' => "جاري تحضير طلبك رقم {$orderNumber}",
                'en' => "Your order {$orderNumber} is being prepared",
            ],
            'ready' => [
                'ar' => "طلبك رقم {$orderNumber} جاهز للاستلام!",
                'en' => "Your order {$orderNumber} is ready for pickup!",
            ],
            'out_for_delivery' => [
                'ar' => "طلبك رقم {$orderNumber} في طريقه إليك",
                'en' => "Your order {$orderNumber} is on its way",
            ],
            'cancelled' => [
                'ar' => "تم إلغاء طلبك رقم {$orderNumber}",
                'en' => "Your order {$orderNumber} has been cancelled",
            ],
            default => [
                'ar' => "تحديث على طلبك رقم {$orderNumber}",
                'en' => "Update on your order {$orderNumber}",
            ],
        };
    }

    private function sendPushNotification(string $token, string $title, string $body, ?array $data = null): void
    {
        try {
            // Firebase Cloud Messaging integration
            // Using kreait/laravel-firebase package
            if (class_exists(\Kreait\Firebase\Messaging::class)) {
                $messaging = app('firebase.messaging');
                $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $token)
                    ->withNotification(\Kreait\Firebase\Messaging\Notification::create($title, $body))
                    ->withData($data ?? []);
                $messaging->send($message);
            }
        } catch (\Throwable $e) {
            Log::error('FCM push failed', ['error' => $e->getMessage(), 'token' => $token]);
        }
    }

    private function sendPushToMultiple(array $tokens, string $titleAr, string $titleEn, string $bodyAr, string $bodyEn, ?array $data = null): void
    {
        try {
            if (class_exists(\Kreait\Firebase\Messaging::class)) {
                $messaging = app('firebase.messaging');
                // Send to topic or individual tokens
                foreach (array_chunk($tokens, 500) as $chunk) {
                    $message = \Kreait\Firebase\Messaging\CloudMessage::new()
                        ->withNotification(\Kreait\Firebase\Messaging\Notification::create($titleAr, $bodyAr))
                        ->withData($data ?? []);
                    $messaging->sendMulticast($message, $chunk);
                }
            }
        } catch (\Throwable $e) {
            Log::error('FCM multicast failed', ['error' => $e->getMessage()]);
        }
    }
}
