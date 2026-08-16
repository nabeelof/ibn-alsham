<?php

namespace App\Models;

use App\Support\Permissions;
use Illuminate\Database\Eloquent\Model;

class Role extends Model
{
    protected $fillable = [
        'key',
        'name_ar',
        'name_en',
        'description_ar',
        'is_system',
    ];

    protected function casts(): array
    {
        return [
            'is_system' => 'boolean',
        ];
    }

    public function permissions()
    {
        return $this->hasMany(RolePermission::class);
    }

    public function admins()
    {
        return $this->hasMany(Admin::class);
    }

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }

    /** The super admin role implicitly holds every permission. */
    public function getIsSuperAttribute(): bool
    {
        return $this->key === 'super_admin';
    }

    public function permissionKeys(): array
    {
        if ($this->is_super) {
            return Permissions::all();
        }

        return $this->permissions->pluck('permission')->all();
    }

    public function hasPermission(string $permission): bool
    {
        return $this->is_super || $this->permissions->contains('permission', $permission);
    }

    /** Replace the role's permission set in one call. */
    public function syncPermissions(array $permissions): void
    {
        $permissions = array_values(array_intersect($permissions, Permissions::all()));

        $this->permissions()->delete();
        $this->permissions()->createMany(
            array_map(fn ($permission) => ['permission' => $permission], $permissions)
        );

        $this->load('permissions');
    }
}
