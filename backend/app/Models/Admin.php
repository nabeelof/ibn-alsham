<?php

namespace App\Models;

use App\Support\Permissions;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class Admin extends Authenticatable
{
    use Notifiable;

    protected $table = 'admins';

    protected $fillable = [
        'name',
        'email',
        'phone',
        'password',
        'role',
        'role_id',
        'is_active',
        'last_login_at',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected function casts(): array
    {
        return [
            'password' => 'hashed',
            'is_active' => 'boolean',
            'last_login_at' => 'datetime',
        ];
    }

    // ─── Relationships ───────────────────────────────────────

    public function roleModel()
    {
        return $this->belongsTo(Role::class, 'role_id');
    }

    // ─── Scopes ──────────────────────────────────────────────

    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }

    // ─── Roles & permissions ─────────────────────────────────

    public function isSuperAdmin(): bool
    {
        return $this->role === 'super_admin' || $this->roleModel?->is_super === true;
    }

    /**
     * Permission check used by every admin screen.
     *
     * Falls back to the legacy `role` string for admins created before roles
     * became rows, so nobody is locked out by the upgrade.
     */
    public function hasPermission(string $permission): bool
    {
        if (!$this->is_active) {
            return false;
        }

        if ($this->isSuperAdmin()) {
            return true;
        }

        if ($this->roleModel) {
            return $this->roleModel->hasPermission($permission);
        }

        return in_array($permission, $this->legacyPermissions(), true);
    }

    public function hasAnyPermission(array $permissions): bool
    {
        foreach ($permissions as $permission) {
            if ($this->hasPermission($permission)) {
                return true;
            }
        }

        return false;
    }

    public function permissionKeys(): array
    {
        if ($this->isSuperAdmin()) {
            return Permissions::all();
        }

        return $this->roleModel?->permissionKeys() ?? $this->legacyPermissions();
    }

    public function roleLabel(): string
    {
        return $this->roleModel?->name
            ?? Permissions::presets()[$this->role]['name_ar']
            ?? $this->role;
    }

    private function legacyPermissions(): array
    {
        return Permissions::presets()[$this->role]['permissions'] ?? [];
    }
}
