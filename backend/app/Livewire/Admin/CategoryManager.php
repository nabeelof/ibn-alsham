<?php

namespace App\Livewire\Admin;

use App\Models\Category;
use Illuminate\Support\Facades\Storage;
use App\Support\Permissions;
use Livewire\Attributes\Rule;
use Livewire\WithFileUploads;
use Livewire\WithPagination;

class CategoryManager extends AdminPage
{
    protected string $permission = Permissions::CATEGORIES_MANAGE;

    use WithPagination, WithFileUploads;

    public string $search = '';
    public string $formMode = ''; // 'create' | 'edit'
    public ?int $editingCategoryId = null;

    #[Rule('required|min:2')]
    public string $nameAr = '';

    #[Rule('required|min:2')]
    public string $nameEn = '';

    #[Rule('nullable|integer|min:0')]
    public string $sortOrder = '0';

    public bool $isActive = true;
    public $image = null;

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function showCreateForm(): void
    {
        $this->resetForm();
        $this->formMode = 'create';
    }

    public function showEditForm(int $categoryId): void
    {
        $category = Category::findOrFail($categoryId);
        $this->editingCategoryId = $categoryId;
        $this->nameAr = $category->name_ar;
        $this->nameEn = $category->name_en;
        $this->sortOrder = (string) $category->sort_order;
        $this->isActive = $category->is_active;
        $this->formMode = 'edit';
    }

    public function save(): void
    {
        $this->validate();

        $data = [
            'name_ar' => $this->nameAr,
            'name_en' => $this->nameEn,
            'sort_order' => (int) ($this->sortOrder ?: 0),
            'is_active' => $this->isActive,
        ];

        if ($this->image) {
            $data['image'] = $this->image->store('categories', 'public');
        }

        if ($this->formMode === 'create') {
            Category::create($data);
            session()->flash('message', 'تم إضافة القسم بنجاح.');
        } else {
            $category = Category::findOrFail($this->editingCategoryId);
            if ($this->image && $category->image) {
                Storage::disk('public')->delete($category->image);
            }
            $category->update($data);
            session()->flash('message', 'تم تحديث القسم بنجاح.');
        }

        $this->resetForm();
    }

    public function toggleActive(int $categoryId): void
    {
        $category = Category::findOrFail($categoryId);
        $category->update(['is_active' => ! $category->is_active]);
    }

    public function deleteCategory(int $categoryId): void
    {
        $category = Category::findOrFail($categoryId);
        if ($category->products()->exists()) {
            session()->flash('message', 'لا يمكن حذف قسم يحتوي على أصناف.');
            return;
        }
        if ($category->image) {
            Storage::disk('public')->delete($category->image);
        }
        $category->delete();
        session()->flash('message', 'تم حذف القسم.');
    }

    public function closeForm(): void
    {
        $this->resetForm();
    }

    private function resetForm(): void
    {
        $this->formMode = '';
        $this->editingCategoryId = null;
        $this->nameAr = $this->nameEn = '';
        $this->sortOrder = '0';
        $this->isActive = true;
        $this->image = null;
        $this->resetValidation();
    }

    public function render()
    {
        return view('livewire.admin.category-manager', [
            'categories' => Category::withCount('products')
                ->when($this->search, fn ($q) => $q->where('name_ar', 'like', "%{$this->search}%")->orWhere('name_en', 'like', "%{$this->search}%"))
                ->orderBy('sort_order')
                ->orderBy('name_ar')
                ->paginate(12),
        ])->title('إدارة الأقسام');
    }
}
