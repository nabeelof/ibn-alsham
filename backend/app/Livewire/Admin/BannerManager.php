<?php

namespace App\Livewire\Admin;

use App\Models\Banner;
use App\Models\Category;
use App\Models\Product;
use App\Support\Permissions;
use Livewire\WithFileUploads;

class BannerManager extends AdminPage
{
    use WithFileUploads;

    protected string $permission = Permissions::BANNERS_MANAGE;

    public ?int $editingId = null;
    public bool $showForm = false;

    public string $titleAr = '';
    public string $titleEn = '';
    public string $linkType = 'none';
    public string $linkValue = '';
    public int $sortOrder = 0;
    public bool $isActive = true;
    public $imageFile = null;

    protected function rules(): array
    {
        return [
            'titleAr' => 'required|string|max:150',
            'titleEn' => 'required|string|max:150',
            'linkType' => 'required|in:none,product,category',
            'linkValue' => 'nullable|string|max:60',
            'sortOrder' => 'integer|min:0',
            'imageFile' => 'nullable|image|max:4096',
        ];
    }

    protected array $validationAttributes = [
        'titleAr' => 'العنوان بالعربية',
        'titleEn' => 'العنوان بالإنجليزية',
        'imageFile' => 'الصورة',
    ];

    public function openForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->reset('imageFile');
        $this->editingId = $id;

        if ($id) {
            $banner = Banner::findOrFail($id);
            $this->titleAr = $banner->title_ar;
            $this->titleEn = $banner->title_en;
            $this->linkType = $banner->link_type ?? 'none';
            $this->linkValue = (string) ($banner->link_value ?? '');
            $this->sortOrder = $banner->sort_order;
            $this->isActive = $banner->is_active;
        } else {
            $this->titleAr = '';
            $this->titleEn = '';
            $this->linkType = 'none';
            $this->linkValue = '';
            $this->sortOrder = Banner::max('sort_order') + 1;
            $this->isActive = true;
        }

        $this->showForm = true;
    }

    public function save(): void
    {
        $data = $this->validate();

        $payload = [
            'title_ar' => $data['titleAr'],
            'title_en' => $data['titleEn'],
            'link_type' => $data['linkType'],
            'link_value' => $data['linkType'] === 'none' ? null : ($data['linkValue'] ?: null),
            'sort_order' => $data['sortOrder'],
            'is_active' => $this->isActive,
        ];

        if ($this->imageFile) {
            $payload['image'] = $this->imageFile->store('banners', 'public');
        }

        Banner::updateOrCreate(['id' => $this->editingId], $payload);

        $this->showForm = false;
        session()->flash('message', $this->editingId ? 'تم تحديث البانر' : 'تمت إضافة البانر');
        $this->editingId = null;
    }

    public function toggleActive(int $id): void
    {
        $banner = Banner::findOrFail($id);
        $banner->update(['is_active' => !$banner->is_active]);
    }

    public function delete(int $id): void
    {
        Banner::findOrFail($id)->delete();
        session()->flash('message', 'تم حذف البانر');
    }

    public function render()
    {
        return view('livewire.admin.banner-manager', [
            'banners' => Banner::orderBy('sort_order')->get(),
            'products' => Product::orderBy('name_ar')->get(['id', 'name_ar']),
            'categories' => Category::orderBy('name_ar')->get(['id', 'name_ar']),
        ])->title('البانرات');
    }
}
