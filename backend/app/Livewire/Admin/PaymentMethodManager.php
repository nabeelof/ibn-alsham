<?php

namespace App\Livewire\Admin;

use App\Models\PaymentMethod;
use App\Support\Permissions;

class PaymentMethodManager extends AdminPage
{
    protected string $permission = Permissions::PAYMENTS_MANAGE;

    public ?int $editingId = null;
    public bool $showForm = false;

    public string $nameAr = '';
    public string $nameEn = '';
    public string $descriptionAr = '';
    public string $descriptionEn = '';
    public string $accountAlias = '';
    public bool $requiresReference = false;
    public bool $availableForPickup = true;
    public bool $availableForDelivery = true;
    public bool $isActive = true;
    public int $sortOrder = 0;

    protected function rules(): array
    {
        return [
            'nameAr' => 'required|string|max:100',
            'nameEn' => 'required|string|max:100',
            'descriptionAr' => 'nullable|string|max:255',
            'descriptionEn' => 'nullable|string|max:255',
            'accountAlias' => 'nullable|string|max:100',
            'sortOrder' => 'integer|min:0',
        ];
    }

    protected array $validationAttributes = [
        'nameAr' => 'الاسم بالعربية',
        'nameEn' => 'الاسم بالإنجليزية',
    ];

    public function openForm(int $id): void
    {
        $this->resetValidation();
        $method = PaymentMethod::findOrFail($id);

        $this->editingId = $id;
        $this->nameAr = $method->name_ar;
        $this->nameEn = $method->name_en;
        $this->descriptionAr = $method->description_ar ?? '';
        $this->descriptionEn = $method->description_en ?? '';
        $this->accountAlias = $method->account_alias ?? '';
        $this->requiresReference = $method->requires_reference;
        $this->availableForPickup = $method->available_for_pickup;
        $this->availableForDelivery = $method->available_for_delivery;
        $this->isActive = $method->is_active;
        $this->sortOrder = $method->sort_order;

        $this->showForm = true;
    }

    public function save(): void
    {
        $data = $this->validate();

        PaymentMethod::findOrFail($this->editingId)->update([
            'name_ar' => $data['nameAr'],
            'name_en' => $data['nameEn'],
            'description_ar' => $data['descriptionAr'] ?: null,
            'description_en' => $data['descriptionEn'] ?: null,
            'account_alias' => $data['accountAlias'] ?: null,
            'requires_reference' => $this->requiresReference,
            'available_for_pickup' => $this->availableForPickup,
            'available_for_delivery' => $this->availableForDelivery,
            'is_active' => $this->isActive,
            'sort_order' => $data['sortOrder'],
        ]);

        $this->showForm = false;
        session()->flash('message', 'تم تحديث وسيلة الدفع');
        $this->editingId = null;
    }

    public function toggleActive(int $id): void
    {
        $method = PaymentMethod::findOrFail($id);

        // The app must always offer at least one way to pay.
        if ($method->is_active && PaymentMethod::where('is_active', true)->count() <= 1) {
            session()->flash('error', 'يجب إبقاء وسيلة دفع واحدة مفعّلة على الأقل');
            return;
        }

        $method->update(['is_active' => !$method->is_active]);
        session()->flash('message', 'تم تحديث حالة وسيلة الدفع');
    }

    public function render()
    {
        return view('livewire.admin.payment-method-manager', [
            'methods' => PaymentMethod::orderBy('sort_order')->get(),
        ])->title('وسائل الدفع');
    }
}
