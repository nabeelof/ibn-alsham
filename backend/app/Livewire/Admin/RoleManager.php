<?php

namespace App\Livewire\Admin;

use App\Models\Role;
use App\Support\Permissions;
use Illuminate\Support\Str;

class RoleManager extends AdminPage
{
    protected string $permission = Permissions::ROLES_MANAGE;

    public ?int $editingId = null;
    public bool $showForm = false;

    public string $nameAr = '';
    public string $nameEn = '';
    public string $descriptionAr = '';
    public array $selectedPermissions = [];

    protected function rules(): array
    {
        return [
            'nameAr' => 'required|string|max:100',
            'nameEn' => 'required|string|max:100',
            'descriptionAr' => 'nullable|string|max:255',
        ];
    }

    protected array $validationAttributes = [
        'nameAr' => 'اسم الدور بالعربية',
        'nameEn' => 'اسم الدور بالإنجليزية',
    ];

    public function openForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->editingId = $id;

        if ($id) {
            $role = Role::with('permissions')->findOrFail($id);
            $this->nameAr = $role->name_ar;
            $this->nameEn = $role->name_en;
            $this->descriptionAr = $role->description_ar ?? '';
            $this->selectedPermissions = $role->permissions->pluck('permission')->all();
        } else {
            $this->nameAr = '';
            $this->nameEn = '';
            $this->descriptionAr = '';
            $this->selectedPermissions = [];
        }

        $this->showForm = true;
    }

    /** Tick or untick every permission in one group. */
    public function toggleGroup(string $group): void
    {
        $keys = array_keys(Permissions::groups()[$group] ?? []);
        $allSelected = empty(array_diff($keys, $this->selectedPermissions));

        $this->selectedPermissions = $allSelected
            ? array_values(array_diff($this->selectedPermissions, $keys))
            : array_values(array_unique(array_merge($this->selectedPermissions, $keys)));
    }

    public function save(): void
    {
        $data = $this->validate();

        $role = Role::updateOrCreate(
            ['id' => $this->editingId],
            [
                'key' => $this->editingId
                    ? Role::find($this->editingId)->key
                    : Str::slug($this->nameEn, '_') . '_' . Str::lower(Str::random(4)),
                'name_ar' => $data['nameAr'],
                'name_en' => $data['nameEn'],
                'description_ar' => $data['descriptionAr'] ?: null,
            ]
        );

        // The super admin role always holds everything; editing it is a no-op.
        if (!$role->is_super) {
            $role->load('permissions');
            $role->syncPermissions($this->selectedPermissions);
        }

        $this->showForm = false;
        session()->flash('message', $this->editingId ? 'تم تحديث الدور' : 'تم إنشاء الدور');
        $this->editingId = null;
    }

    public function delete(int $id): void
    {
        $role = Role::withCount('admins')->findOrFail($id);

        if ($role->is_system) {
            session()->flash('error', 'لا يمكن حذف الأدوار الأساسية');
            return;
        }

        if ($role->admins_count > 0) {
            session()->flash('error', 'لا يمكن حذف دور مرتبط بمشرفين. انقل المشرفين إلى دور آخر أولاً.');
            return;
        }

        $role->delete();
        session()->flash('message', 'تم حذف الدور');
    }

    public function render()
    {
        return view('livewire.admin.role-manager', [
            'roles' => Role::withCount(['admins', 'permissions'])->orderByDesc('is_system')->get(),
            'groups' => Permissions::groups(),
        ])->title('الأدوار والصلاحيات');
    }
}
