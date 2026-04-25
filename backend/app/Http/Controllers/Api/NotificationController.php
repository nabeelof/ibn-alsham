<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Notification;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    use ApiResponse;

    public function index(Request $request)
    {
        $locale = app()->getLocale();

        $notifications = Notification::where('user_id', $request->user()->id)
            ->latest()
            ->paginate(20);

        return $this->success($notifications->map(fn ($n) => [
            'id' => $n->id,
            'title' => $n->{"title_{$locale}"},
            'body' => $n->{"body_{$locale}"},
            'is_read' => $n->is_read,
            'created_at' => $n->created_at->diffForHumans(),
        ]));
    }

    public function markAllRead(Request $request)
    {
        Notification::where('user_id', $request->user()->id)
            ->where('is_read', false)
            ->update(['is_read' => true]);

        return $this->success(message: 'تم تعيين جميع الإشعارات كمقروءة');
    }

    public function unreadCount(Request $request)
    {
        $count = Notification::where('user_id', $request->user()->id)
            ->where('is_read', false)
            ->count();

        return $this->success(['unread_count' => $count]);
    }

    public function bulkDelete(Request $request)
    {
        $ids = $request->input('ids', []);
        $query = Notification::where('user_id', $request->user()->id);

        if (is_array($ids) && count($ids) > 0) {
            $query->whereIn('id', $ids)->delete();
        } else {
            $query->delete();
        }

        return $this->success(['deleted' => true]);
    }
}
