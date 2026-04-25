<?php

namespace App\Models;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class Admin extends Authenticatable
{
    use Notifiable;

    protected $table = 'admins';

    protected $fillable = [
        'name',
        'email',
        'password',
        'role',
        'is_active',
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
        ];
    }

    // ─── Role Checks ─────────────────────────────────────────

    public function isSuperAdmin(): bool
    {
        return $this->role === 'super_admin';
    }

    public function isContentManager(): bool
    {
        return $this->role === 'content_manager';
    }

    public function isOrderManager(): bool
    {
        return $this->role === 'order_manager';
    }

    public function canManageContent(): bool
    {
        return $this->isSuperAdmin() || $this->isContentManager();
    }

    public function canManageOrders(): bool
    {
        return $this->isSuperAdmin() || $this->isOrderManager();
    }
}
