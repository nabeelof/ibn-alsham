<?php

namespace App\Livewire\Admin;

use App\Models\Product;
use App\Models\ProductOption;
use App\Models\ProductOptionGroup;
use App\Support\Permissions;

class OptionGroupManager extends AdminPage
{
    protected string $permission = Permissions::OPTIONS_MANAGE;

    public ?int $productId = null;

    // Group form
    public bool $showGroupForm = false;
    public ?int $editingGroupId = null;
    public string $groupNameAr = '';
    public string $groupNameEn = '';
    public string $groupType = 'checkbox';
    public bool $groupRequired = false;
    public int $groupMin = 0;
    public string $groupMax = '';
    public int $groupSort = 0;

    // Option form
    public bool $showOptionForm = false;
    public ?int $editingOptionId = null;
    public ?int $optionGroupId = null;
    public string $optionNameAr = '';
    public string $optionNameEn = '';
    public string $optionPrice = '0';
    public bool $optionDefault = false;
    public bool $optionChangeable = true;

    protected function initPage(): void
    {
        $this->productId = Product::orderBy('name_ar')->value('id');
    }

    // ─── Groups ──────────────────────────────────────────────

    public function openGroupForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->editingGroupId = $id;

        if ($id) {
            $group = ProductOptionGroup::findOrFail($id);
            $this->groupNameAr = $group->name_ar;
            $this->groupNameEn = $group->name_en;
            $this->groupType = $group->type;
            $this->groupRequired = $group->is_required;
            $this->groupMin = $group->min_selections;
            $this->groupMax = $group->max_selections !== null ? (string) $group->max_selections : '';
            $this->groupSort = $group->sort_order;
        } else {
            $this->groupNameAr = '';
            $this->groupNameEn = '';
            $this->groupType = 'checkbox';
            $this->groupRequired = false;
            $this->groupMin = 0;
            $this->groupMax = '';
            $this->groupSort = ProductOptionGroup::where('product_id', $this->productId)->max('sort_order') + 1;
        }

        $this->showGroupForm = true;
    }

    public function saveGroup(): void
    {
        $data = $this->validate([
            'groupNameAr' => 'required|string|max:100',
            'groupNameEn' => 'required|string|max:100',
            'groupType' => 'required|in:checkbox,radio',
            'groupMin' => 'integer|min:0',
            'groupMax' => 'nullable|integer|min:1',
            'productId' => 'required|exists:products,id',
        ], [], [
            'groupNameAr' => 'اسم المجموعة',
            'groupNameEn' => 'الاسم بالإنجليزية',
        ]);

        // A radio group is a single choice by definition.
        $max = $data['groupType'] === 'radio' ? 1 : ($data['groupMax'] !== '' ? $data['groupMax'] : null);
        $min = $data['groupType'] === 'radio' && $this->groupRequired ? 1 : $data['groupMin'];

        ProductOptionGroup::updateOrCreate(
            ['id' => $this->editingGroupId],
            [
                'product_id' => $data['productId'],
                'name_ar' => $data['groupNameAr'],
                'name_en' => $data['groupNameEn'],
                'type' => $data['groupType'],
                'is_required' => $this->groupRequired,
                'is_skippable' => !$this->groupRequired,
                'min_selections' => $min,
                'max_selections' => $max,
                'sort_order' => $this->groupSort,
                'is_active' => true,
            ]
        );

        $this->showGroupForm = false;
        session()->flash('message', $this->editingGroupId ? 'تم تحديث المجموعة' : 'تمت إضافة المجموعة');
        $this->editingGroupId = null;
    }

    public function deleteGroup(int $id): void
    {
        ProductOptionGroup::findOrFail($id)->delete();
        session()->flash('message', 'تم حذف المجموعة وخياراتها');
    }

    // ─── Options ─────────────────────────────────────────────

    public function openOptionForm(int $groupId, ?int $optionId = null): void
    {
        $this->resetValidation();
        $this->optionGroupId = $groupId;
        $this->editingOptionId = $optionId;

        if ($optionId) {
            $option = ProductOption::findOrFail($optionId);
            $this->optionNameAr = $option->name_ar;
            $this->optionNameEn = $option->name_en;
            $this->optionPrice = (string) $option->price;
            $this->optionDefault = $option->default_selected;
            $this->optionChangeable = $option->is_user_changeable;
        } else {
            $this->optionNameAr = '';
            $this->optionNameEn = '';
            $this->optionPrice = '0';
            $this->optionDefault = false;
            $this->optionChangeable = true;
        }

        $this->showOptionForm = true;
    }

    public function saveOption(): void
    {
        $data = $this->validate([
            'optionNameAr' => 'required|string|max:100',
            'optionNameEn' => 'required|string|max:100',
            'optionPrice' => 'required|numeric|min:0',
        ], [], [
            'optionNameAr' => 'اسم الخيار',
            'optionNameEn' => 'الاسم بالإنجليزية',
            'optionPrice' => 'السعر',
        ]);

        $group = ProductOptionGroup::findOrFail($this->optionGroupId);

        ProductOption::updateOrCreate(
            ['id' => $this->editingOptionId],
            [
                'product_id' => $group->product_id,
                'option_group_id' => $group->id,
                'name_ar' => $data['optionNameAr'],
                'name_en' => $data['optionNameEn'],
                'price' => $data['optionPrice'],
                'is_required' => false,
                'default_selected' => $this->optionDefault,
                'is_user_changeable' => $this->optionChangeable,
                'is_active' => true,
                'sort_order' => ProductOption::where('option_group_id', $group->id)->max('sort_order') + 1,
            ]
        );

        $this->showOptionForm = false;
        session()->flash('message', $this->editingOptionId ? 'تم تحديث الخيار' : 'تمت إضافة الخيار');
        $this->editingOptionId = null;
    }

    public function deleteOption(int $id): void
    {
        ProductOption::findOrFail($id)->delete();
        session()->flash('message', 'تم حذف الخيار');
    }

    public function render()
    {
        $groups = $this->productId
            ? ProductOptionGroup::with(['options' => fn ($q) => $q->orderBy('sort_order')])
                ->where('product_id', $this->productId)
                ->orderBy('sort_order')
                ->get()
            : collect();

        return view('livewire.admin.option-group-manager', [
            'products' => Product::orderBy('name_ar')->get(['id', 'name_ar']),
            'groups' => $groups,
        ])->title('الإضافات والخيارات');
    }
}
