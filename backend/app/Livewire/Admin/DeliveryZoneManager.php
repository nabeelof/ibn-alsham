<?php

namespace App\Livewire\Admin;

use App\Models\DeliveryZone;
use App\Support\Permissions;

class DeliveryZoneManager extends AdminPage
{
    protected string $permission = Permissions::DELIVERY_MANAGE;

    public ?int $editingId = null;
    public bool $showForm = false;

    public string $nameAr = '';
    public string $nameEn = '';
    public string $minKm = '0';
    public string $maxKm = '';
    public string $basePrice = '';
    public string $pricePerKm = '0';
    public bool $isActive = true;

    protected function rules(): array
    {
        return [
            'nameAr' => 'required|string|max:100',
            'nameEn' => 'required|string|max:100',
            'minKm' => 'required|numeric|min:0',
            'maxKm' => 'required|numeric|gt:minKm',
            'basePrice' => 'required|numeric|min:0',
            'pricePerKm' => 'required|numeric|min:0',
        ];
    }

    protected array $validationAttributes = [
        'nameAr' => 'اسم المنطقة',
        'nameEn' => 'الاسم بالإنجليزية',
        'minKm' => 'من (كم)',
        'maxKm' => 'إلى (كم)',
        'basePrice' => 'السعر الأساسي',
        'pricePerKm' => 'سعر الكيلومتر',
    ];

    public function openForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->editingId = $id;

        if ($id) {
            $zone = DeliveryZone::findOrFail($id);
            $this->nameAr = $zone->name_ar;
            $this->nameEn = $zone->name_en;
            $this->minKm = (string) $zone->min_km;
            $this->maxKm = (string) $zone->max_km;
            $this->basePrice = (string) $zone->base_price;
            $this->pricePerKm = (string) $zone->price_per_km;
            $this->isActive = $zone->is_active;
        } else {
            $this->nameAr = '';
            $this->nameEn = '';
            $this->minKm = (string) (DeliveryZone::max('max_km') ?? 0);
            $this->maxKm = '';
            $this->basePrice = '';
            $this->pricePerKm = '0';
            $this->isActive = true;
        }

        $this->showForm = true;
    }

    public function save(): void
    {
        $data = $this->validate();

        DeliveryZone::updateOrCreate(
            ['id' => $this->editingId],
            [
                'name_ar' => $data['nameAr'],
                'name_en' => $data['nameEn'],
                'min_km' => $data['minKm'],
                'max_km' => $data['maxKm'],
                'base_price' => $data['basePrice'],
                'price_per_km' => $data['pricePerKm'],
                'is_active' => $this->isActive,
            ]
        );

        $this->showForm = false;
        session()->flash('message', $this->editingId ? 'تم تحديث المنطقة' : 'تمت إضافة المنطقة');
        $this->editingId = null;
    }

    public function delete(int $id): void
    {
        DeliveryZone::findOrFail($id)->delete();
        session()->flash('message', 'تم حذف المنطقة');
    }

    public function render()
    {
        return view('livewire.admin.delivery-zone-manager', [
            'zones' => DeliveryZone::orderBy('min_km')->get(),
        ])->title('مناطق التوصيل');
    }
}
