<?php

namespace App\Livewire\Admin;

use App\Models\Admin;
use App\Models\Role;
use App\Support\Permissions;
use Illuminate\Validation\Rule;
use Livewire\WithPagination;

class AdminManager extends AdminPage
{
    use WithPagination;

    protected string $permission = Permissions::ADMINS_MANAGE;

    public string $search = '';

    public ?int $editingId = null;
    public bool $showForm = false;

    public string $name = '';
    public string $email = '';
    public string $phone = '';
    public string $password = '';
    public ?int $roleId = null;
    public bool $isActive = true;

    protected function rules(): array
    {
        return [
            'name' => 'required|string|max:100',
            'email' => ['required', 'email', 'max:150', Rule::unique('admins', 'email')->ignore($this->editingId)],
            'phone' => 'nullable|string|max:30',
            'roleId' => 'required|exists:roles,id',
            // Password is required for new admins, optional when editing.
            'password' => $this->editingId ? 'nullable|string|min:8' : 'required|string|min:8',
        ];
    }

    protected array $validationAttributes = [
        'name' => 'الاسم',
        'email' => 'البريد الإلكتروني',
        'roleId' => 'الدور',
        'password' => 'كلمة المرور',
    ];

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function openForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->editingId = $id;
        $this->password = '';

        if ($id) {
            $admin = Admin::findOrFail($id);
            $this->name = $admin->name;
            $this->email = $admin->email;
            $this->phone = $admin->phone ?? '';
            $this->roleId = $admin->role_id;
            $this->isActive = $admin->is_active;
        } else {
            $this->name = '';
            $this->email = '';
            $this->phone = '';
            $this->roleId = Role::where('key', 'order_manager')->value('id');
            $this->isActive = true;
        }

        $this->showForm = true;
    }

    public function save(): void
    {
        $data = $this->validate();
        $role = Role::findOrFail($data['roleId']);

        $payload = [
            'name' => $data['name'],
            'email' => $data['email'],
            'phone' => $data['phone'] ?: null,
            'role_id' => $role->id,
            // Keep the legacy string column in sync for older code paths.
            'role' => $role->key,
            'is_active' => $this->isActive,
        ];

        if (filled($this->password)) {
            $payload['password'] = $this->password;
        }

        Admin::updateOrCreate(['id' => $this->editingId], $payload);

        $this->showForm = false;
        session()->flash('message', $this->editingId ? 'تم تحديث المشرف' : 'تمت إضافة المشرف');
        $this->editingId = null;
    }

    public function toggleActive(int $id): void
    {
        $admin = Admin::findOrFail($id);

        if ($admin->id === auth('admin')->id()) {
            session()->flash('error', 'لا يمكنك تعطيل حسابك الخاص');
            return;
        }

        $admin->update(['is_active' => !$admin->is_active]);
        session()->flash('message', $admin->is_active ? 'تم تفعيل الحساب' : 'تم تعطيل الحساب');
    }

    public function delete(int $id): void
    {
        if ($id === auth('admin')->id()) {
            session()->flash('error', 'لا يمكنك حذف حسابك الخاص');
            return;
        }

        $admin = Admin::findOrFail($id);

        // Never leave the panel without a way back in.
        if ($admin->isSuperAdmin() && Admin::where('role', 'super_admin')->count() <= 1) {
            session()->flash('error', 'لا يمكن حذف المدير العام الوحيد');
            return;
        }

        $admin->delete();
        session()->flash('message', 'تم حذف المشرف');
    }

    public function render()
    {
        $admins = Admin::with('roleModel')
            ->when($this->search, fn ($q) => $q->where(function ($sub) {
                $sub->where('name', 'like', "%{$this->search}%")
                    ->orWhere('email', 'like', "%{$this->search}%");
            }))
            ->latest()
            ->paginate(15);

        return view('livewire.admin.admin-manager', [
            'admins' => $admins,
            'roles' => Role::orderBy('name_ar')->get(),
            'currentAdminId' => auth('admin')->id(),
        ])->title('المشرفون');
    }
}
