<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Notification;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    use ApiResponse;

    public function index(Request $request): JsonResponse
    {
        $locale = app()->getLocale();

        $notifications = Notification::where('user_id', $request->user()->id)
            ->latest()
            ->paginate((int) $request->input('per_page', 20));

        return $this->success(
            $notifications->map(fn ($n) => $this->format($n, $locale)),
            [
                'current_page' => $notifications->currentPage(),
                'last_page' => $notifications->lastPage(),
                'total' => $notifications->total(),
            ]
        );
    }

    public function markAllRead(Request $request): JsonResponse
    {
        Notification::where('user_id', $request->user()->id)
            ->whereNull('read_at')
            ->update(['read_at' => now()]);

        return $this->success(['result' => true], message: __('notifications.all_read'));
    }

    public function markRead(int $id, Request $request): JsonResponse
    {
        $notification = Notification::where('user_id', $request->user()->id)->findOrFail($id);
        $notification->markAsRead();

        return $this->success($this->format($notification->fresh(), app()->getLocale()));
    }

    public function unreadCount(Request $request): JsonResponse
    {
        $count = Notification::where('user_id', $request->user()->id)
            ->whereNull('read_at')
            ->count();

        return $this->success(['unread_count' => $count]);
    }

    public function bulkDelete(Request $request): JsonResponse
    {
        $ids = $request->input('ids', []);
        $query = Notification::where('user_id', $request->user()->id);

        if (is_array($ids) && count($ids) > 0) {
            $query->whereIn('id', $ids);
        }

        $deleted = $query->delete();

        return $this->success(['deleted' => $deleted]);
    }

    private function format(Notification $n, string $locale): array
    {
        return [
            'id' => $n->id,
            'title' => $n->{"title_{$locale}"},
            'body' => $n->{"body_{$locale}"},
            'type' => $n->type,
            'data' => $n->data,
            'is_read' => $n->read_at !== null,
            'created_at' => $n->created_at->toIso8601String(),
            'created_at_human' => $n->created_at->diffForHumans(),
        ];
    }
}
