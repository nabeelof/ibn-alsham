<?php

namespace App\Livewire\Admin;

use App\Models\Notification;
use App\Models\User;
use App\Services\NotificationService;
use App\Support\Permissions;
use Livewire\WithPagination;

class NotificationComposer extends AdminPage
{
    use WithPagination;

    protected string $permission = Permissions::NOTIFICATIONS_SEND;

    public string $titleAr = '';
    public string $titleEn = '';
    public string $bodyAr = '';
    public string $bodyEn = '';
    public string $audience = 'all';
    public ?int $userId = null;
    public string $type = 'promotion';

    protected function rules(): array
    {
        return [
            'titleAr' => 'required|string|max:150',
            'titleEn' => 'required|string|max:150',
            'bodyAr' => 'required|string|max:500',
            'bodyEn' => 'required|string|max:500',
            'audience' => 'required|in:all,single',
            'userId' => 'nullable|required_if:audience,single|exists:users,id',
            'type' => 'required|in:promotion,general',
        ];
    }

    protected array $validationAttributes = [
        'titleAr' => 'العنوان بالعربية',
        'titleEn' => 'العنوان بالإنجليزية',
        'bodyAr' => 'النص بالعربية',
        'bodyEn' => 'النص بالإنجليزية',
        'userId' => 'الزبون',
    ];

    public function send(): void
    {
        $data = $this->validate();
        $service = app(NotificationService::class);

        if ($data['audience'] === 'single') {
            $user = User::findOrFail($data['userId']);
            $service->sendToUser(
                $user,
                $data['titleAr'], $data['titleEn'],
                $data['bodyAr'], $data['bodyEn'],
                $data['type']
            );
            $message = "تم إرسال الإشعار إلى {$user->name}";
        } else {
            $service->sendToAll(
                $data['titleAr'], $data['titleEn'],
                $data['bodyAr'], $data['bodyEn'],
                $data['type']
            );
            $message = 'تم إرسال الإشعار إلى جميع الزبائن';
        }

        $this->reset(['titleAr', 'titleEn', 'bodyAr', 'bodyEn', 'userId']);
        session()->flash('message', $message);
    }

    public function render()
    {
        return view('livewire.admin.notification-composer', [
            'recent' => Notification::with('user')->latest()->paginate(15),
            'customers' => User::orderBy('name')->get(['id', 'name', 'phone']),
            'reachable' => User::whereNotNull('fcm_token')->count(),
            'totalCustomers' => User::count(),
        ])->title('الإشعارات');
    }
}
