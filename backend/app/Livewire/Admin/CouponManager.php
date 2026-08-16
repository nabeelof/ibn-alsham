<?php

namespace App\Livewire\Admin;

use App\Models\Coupon;
use App\Support\Permissions;
use Illuminate\Validation\Rule;
use Livewire\WithPagination;

class CouponManager extends AdminPage
{
    use WithPagination;

    protected string $permission = Permissions::COUPONS_MANAGE;

    public string $search = '';

    public ?int $editingId = null;
    public bool $showForm = false;

    public string $code = '';
    public string $titleAr = '';
    public string $titleEn = '';
    public string $discountType = 'percent';
    public string $discountValue = '';
    public string $maxDiscount = '';
    public string $minOrderTotal = '0';
    public string $usageLimit = '';
    public string $usageLimitPerUser = '';
    public string $startsAt = '';
    public string $expiresAt = '';
    public bool $isActive = true;

    protected function rules(): array
    {
        return [
            'code' => ['required', 'string', 'max:64', Rule::unique('coupons', 'code')->ignore($this->editingId)],
            'titleAr' => 'required|string|max:150',
            'titleEn' => 'required|string|max:150',
            'discountType' => 'required|in:percent,fixed',
            'discountValue' => 'required|numeric|min:0.001',
            'maxDiscount' => 'nullable|numeric|min:0',
            'minOrderTotal' => 'required|numeric|min:0',
            'usageLimit' => 'nullable|integer|min:1',
            'usageLimitPerUser' => 'nullable|integer|min:1',
            'startsAt' => 'nullable|date',
            'expiresAt' => 'nullable|date|after_or_equal:startsAt',
        ];
    }

    protected array $validationAttributes = [
        'code' => 'كود الخصم',
        'titleAr' => 'الوصف بالعربية',
        'titleEn' => 'الوصف بالإنجليزية',
        'discountValue' => 'قيمة الخصم',
        'minOrderTotal' => 'الحد الأدنى للطلب',
        'expiresAt' => 'تاريخ الانتهاء',
    ];

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function openForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->editingId = $id;

        if ($id) {
            $coupon = Coupon::findOrFail($id);
            $this->code = $coupon->code;
            $this->titleAr = $coupon->title_ar;
            $this->titleEn = $coupon->title_en;
            $this->discountType = $coupon->discount_type;
            $this->discountValue = (string) $coupon->discount_value;
            $this->maxDiscount = $coupon->max_discount !== null ? (string) $coupon->max_discount : '';
            $this->minOrderTotal = (string) $coupon->min_order_total;
            $this->usageLimit = $coupon->usage_limit !== null ? (string) $coupon->usage_limit : '';
            $this->usageLimitPerUser = $coupon->usage_limit_per_user !== null ? (string) $coupon->usage_limit_per_user : '';
            $this->startsAt = $coupon->starts_at?->format('Y-m-d') ?? '';
            $this->expiresAt = $coupon->expires_at?->format('Y-m-d') ?? '';
            $this->isActive = $coupon->is_active;
        } else {
            $this->code = '';
            $this->titleAr = '';
            $this->titleEn = '';
            $this->discountType = 'percent';
            $this->discountValue = '';
            $this->maxDiscount = '';
            $this->minOrderTotal = '0';
            $this->usageLimit = '';
            $this->usageLimitPerUser = '';
            $this->startsAt = '';
            $this->expiresAt = '';
            $this->isActive = true;
        }

        $this->showForm = true;
    }

    public function save(): void
    {
        $data = $this->validate();

        Coupon::updateOrCreate(
            ['id' => $this->editingId],
            [
                'code' => strtoupper(trim($data['code'])),
                'title_ar' => $data['titleAr'],
                'title_en' => $data['titleEn'],
                'discount_type' => $data['discountType'],
                'discount_value' => $data['discountValue'],
                'max_discount' => $data['maxDiscount'] !== '' ? $data['maxDiscount'] : null,
                'min_order_total' => $data['minOrderTotal'],
                'usage_limit' => $data['usageLimit'] !== '' ? $data['usageLimit'] : null,
                'usage_limit_per_user' => $data['usageLimitPerUser'] !== '' ? $data['usageLimitPerUser'] : null,
                'starts_at' => $data['startsAt'] ?: null,
                'expires_at' => $data['expiresAt'] ?: null,
                'is_active' => $this->isActive,
            ]
        );

        $this->showForm = false;
        session()->flash('message', $this->editingId ? 'تم تحديث الكوبون' : 'تمت إضافة الكوبون');
        $this->editingId = null;
    }

    public function toggleActive(int $id): void
    {
        $coupon = Coupon::findOrFail($id);
        $coupon->update(['is_active' => !$coupon->is_active]);
        session()->flash('message', 'تم تحديث حالة الكوبون');
    }

    public function delete(int $id): void
    {
        Coupon::findOrFail($id)->delete();
        session()->flash('message', 'تم حذف الكوبون');
    }

    public function render()
    {
        return view('livewire.admin.coupon-manager', [
            'coupons' => Coupon::withCount('usages')
                ->when($this->search, fn ($q) => $q->where('code', 'like', "%{$this->search}%"))
                ->latest()
                ->paginate(15),
        ])->title('الكوبونات');
    }
}
