<?php

namespace App\Livewire\Admin;

use App\Models\Category;
use App\Models\Product;
use App\Models\ProductOptionGroup;
use App\Models\ProductOption;
use App\Support\Permissions;
use Livewire\Attributes\Rule;
use Livewire\WithFileUploads;
use Livewire\WithPagination;

class MenuManager extends AdminPage
{
    use WithPagination, WithFileUploads;

    protected string $permission = Permissions::PRODUCTS_VIEW;

    public string $search = '';
    public string $categoryFilter = '';
    public string $formMode = ''; // 'create' | 'edit'
    public ?int $editingProductId = null;

    // Product Form Fields
    #[Rule('required|min:2')]
    public string $nameAr = '';

    #[Rule('required|min:2')]
    public string $nameEn = '';

    public string $descriptionAr = '';
    public string $descriptionEn = '';

    #[Rule('required|numeric|min:0')]
    public string $price = '';

    public string $discountedPrice = '';

    /** Purchase cost — feeds the profit calculation in the accounting screen. */
    public string $costPrice = '';

    #[Rule('required|exists:categories,id')]
    public string $categoryId = '';

    public bool $isAvailable = true;
    public $image = null;

    // Option Groups Management
    public ?int $managingOptionsForProductId = null;
    public array $optionGroups = [];

    // New option group form
    public string $newGroupNameAr = '';
    public string $newGroupNameEn = '';
    public string $newGroupType = 'checkbox';
    public bool $newGroupRequired = false;
    public bool $newGroupSkippable = true;

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function showCreateForm(): void
    {
        $this->resetForm();
        $this->formMode = 'create';
    }

    public function showEditForm(int $productId): void
    {
        $product = Product::findOrFail($productId);
        $this->editingProductId = $productId;
        $this->nameAr = $product->name_ar;
        $this->nameEn = $product->name_en;
        $this->descriptionAr = $product->description_ar ?? '';
        $this->descriptionEn = $product->description_en ?? '';
        $this->price = (string) $product->price;
        $this->discountedPrice = (string) ($product->discounted_price ?? '');
        $this->costPrice = (string) ($product->cost_price ?? '');
        $this->categoryId = (string) $product->category_id;
        $this->isAvailable = $product->is_available;
        $this->formMode = 'edit';
    }

    public function save(): void
    {
        $this->validate();

        $data = [
            'name_ar' => $this->nameAr,
            'name_en' => $this->nameEn,
            'description_ar' => $this->descriptionAr,
            'description_en' => $this->descriptionEn,
            'price' => (float) $this->price,
            'discounted_price' => $this->discountedPrice ? (float) $this->discountedPrice : null,
            'cost_price' => $this->costPrice !== '' ? (float) $this->costPrice : null,
            'category_id' => (int) $this->categoryId,
            'is_available' => $this->isAvailable,
        ];

        if ($this->image) {
            $data['image'] = $this->image->store('products', 'public');
        }

        if ($this->formMode === 'create') {
            Product::create($data);
            session()->flash('message', 'تم إضافة الصنف بنجاح.');
        } else {
            Product::findOrFail($this->editingProductId)->update($data);
            session()->flash('message', 'تم تحديث الصنف بنجاح.');
        }

        $this->resetForm();
    }

    public function toggleAvailability(int $productId): void
    {
        $product = Product::findOrFail($productId);
        $product->update(['is_available' => ! $product->is_available]);
    }

    public function deleteProduct(int $productId): void
    {
        Product::findOrFail($productId)->delete();
        session()->flash('message', 'تم حذف الصنف.');
    }

    // ─── Option Groups Management ────────────────────────────

    public function manageOptions(int $productId): void
    {
        $this->managingOptionsForProductId = $productId;
        $this->loadOptionGroups();
    }

    public function closeOptionsManager(): void
    {
        $this->managingOptionsForProductId = null;
        $this->optionGroups = [];
        $this->resetOptionGroupForm();
    }

    public function loadOptionGroups(): void
    {
        if (!$this->managingOptionsForProductId) return;

        $this->optionGroups = ProductOptionGroup::where('product_id', $this->managingOptionsForProductId)
            ->with('allOptions')
            ->orderBy('sort_order')
            ->get()
            ->toArray();
    }

    public function addOptionGroup(): void
    {
        if (!$this->managingOptionsForProductId) return;
        if (empty($this->newGroupNameAr) || empty($this->newGroupNameEn)) {
            session()->flash('error', 'يجب ملء اسم المجموعة بالعربية والإنجليزية');
            return;
        }

        ProductOptionGroup::create([
            'product_id' => $this->managingOptionsForProductId,
            'name_ar' => $this->newGroupNameAr,
            'name_en' => $this->newGroupNameEn,
            'type' => $this->newGroupType,
            'is_required' => $this->newGroupRequired,
            'is_skippable' => $this->newGroupSkippable,
            'sort_order' => count($this->optionGroups),
        ]);

        $this->resetOptionGroupForm();
        $this->loadOptionGroups();
        session()->flash('message', 'تمت إضافة مجموعة الخيارات.');
    }

    public function deleteOptionGroup(int $groupId): void
    {
        ProductOptionGroup::findOrFail($groupId)->delete();
        $this->loadOptionGroups();
        session()->flash('message', 'تم حذف مجموعة الخيارات.');
    }

    public function addOption(int $groupId, string $nameAr, string $nameEn, float $price, bool $defaultSelected, bool $isUserChangeable): void
    {
        $group = ProductOptionGroup::findOrFail($groupId);

        ProductOption::create([
            'product_id' => $group->product_id,
            'option_group_id' => $groupId,
            'name_ar' => $nameAr,
            'name_en' => $nameEn,
            'price' => $price,
            'default_selected' => $defaultSelected,
            'is_user_changeable' => $isUserChangeable,
            'is_active' => true,
            'sort_order' => $group->allOptions()->count(),
        ]);

        $this->loadOptionGroups();
        session()->flash('message', 'تمت إضافة الخيار.');
    }

    public function deleteOption(int $optionId): void
    {
        ProductOption::findOrFail($optionId)->delete();
        $this->loadOptionGroups();
    }

    public function toggleOptionActive(int $optionId): void
    {
        $option = ProductOption::findOrFail($optionId);
        $option->update(['is_active' => !$option->is_active]);
        $this->loadOptionGroups();
    }

    public function closeForm(): void
    {
        $this->resetForm();
    }

    private function resetForm(): void
    {
        $this->formMode = '';
        $this->editingProductId = null;
        $this->nameAr = $this->nameEn = $this->descriptionAr = $this->descriptionEn = '';
        $this->price = $this->discountedPrice = $this->costPrice = $this->categoryId = '';
        $this->isAvailable = true;
        $this->image = null;
        $this->resetValidation();
    }

    private function resetOptionGroupForm(): void
    {
        $this->newGroupNameAr = '';
        $this->newGroupNameEn = '';
        $this->newGroupType = 'checkbox';
        $this->newGroupRequired = false;
        $this->newGroupSkippable = true;
    }

    public function render()
    {
        return view('livewire.admin.menu-manager', [
            'products' => Product::with('category')
                ->when($this->search, fn ($q) => $q->where('name_ar', 'like', "%{$this->search}%")->orWhere('name_en', 'like', "%{$this->search}%"))
                ->when($this->categoryFilter, fn ($q) => $q->where('category_id', $this->categoryFilter))
                ->latest()
                ->paginate(12),
            'categories' => Category::orderBy('sort_order')->get(),
        ])->title('إدارة قائمة الطعام');
    }
}
