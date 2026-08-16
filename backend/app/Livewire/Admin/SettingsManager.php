<?php

namespace App\Livewire\Admin;

use App\Models\Setting;
use App\Support\Permissions;

class SettingsManager extends AdminPage
{
    protected string $permission = Permissions::SETTINGS_MANAGE;

    /** setting key => ['ar' => ..., 'en' => ...] */
    public array $values = [];

    /** Settings the app reads, grouped for the form. */
    public const GROUPS = [
        'general' => [
            'label' => 'معلومات عامة',
            'keys' => [
                'about_us' => ['label' => 'من نحن', 'type' => 'textarea'],
                'announcement' => ['label' => 'شريط الإعلان في الرئيسية', 'type' => 'text'],
                'working_hours' => ['label' => 'ساعات العمل', 'type' => 'text'],
                'min_order_total' => ['label' => 'الحد الأدنى لقيمة الطلب (د.أ)', 'type' => 'text'],
            ],
        ],
        'contact' => [
            'label' => 'التواصل',
            'keys' => [
                'contact_phone' => ['label' => 'رقم الهاتف', 'type' => 'text'],
                'whatsapp' => ['label' => 'واتساب', 'type' => 'text'],
                'address' => ['label' => 'العنوان', 'type' => 'text'],
                'facebook_url' => ['label' => 'فيسبوك', 'type' => 'text'],
                'instagram_url' => ['label' => 'إنستغرام', 'type' => 'text'],
            ],
        ],
        'legal' => [
            'label' => 'الشروط والخصوصية',
            'keys' => [
                'privacy_policy' => ['label' => 'سياسة الخصوصية', 'type' => 'textarea'],
                'terms_conditions' => ['label' => 'الشروط والأحكام', 'type' => 'textarea'],
            ],
        ],
    ];

    protected function initPage(): void
    {
        foreach (self::GROUPS as $group => $config) {
            foreach ($config['keys'] as $key => $meta) {
                $setting = Setting::where('key', $key)->first();
                $this->values[$key] = [
                    'ar' => $setting->value_ar ?? '',
                    'en' => $setting->value_en ?? '',
                ];
            }
        }
    }

    public function save(): void
    {
        foreach ($this->values as $key => $pair) {
            $group = $this->groupFor($key);

            Setting::updateOrCreate(
                ['key' => $key],
                [
                    'value_ar' => $pair['ar'] ?? '',
                    'value_en' => $pair['en'] ?? '',
                    'group' => $group,
                ]
            );
        }

        session()->flash('message', 'تم حفظ الإعدادات');
    }

    private function groupFor(string $key): string
    {
        foreach (self::GROUPS as $group => $config) {
            if (isset($config['keys'][$key])) {
                return $group;
            }
        }

        return 'general';
    }

    public function render()
    {
        return view('livewire.admin.settings-manager', [
            'groups' => self::GROUPS,
        ])->title('إعدادات التطبيق');
    }
}
