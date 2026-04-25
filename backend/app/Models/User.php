<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = [
        'name',
        'phone',
        'otp',
        'otp_expires_at',
        'is_verified',
        'fcm_token',
        'locale',
        'avatar',
    ];

    protected $hidden = [
        'otp',
        'otp_expires_at',
    ];

    protected function casts(): array
    {
        return [
            'otp_expires_at' => 'datetime',
            'is_verified' => 'boolean',
        ];
    }

    /**
     * Check if user is soft-deleted (phone ends with -ي)
     */
    public function isDeleted(): bool
    {
        return str_ends_with($this->phone, '-ي');
    }

    /**
     * Soft delete by appending -ي to phone number
     */
    public function softDeleteAccount(): void
    {
        $this->update([
            'phone' => $this->phone . '-ي',
        ]);
        $this->tokens()->delete();
    }

    // ─── Relationships ───────────────────────────────────────

    public function orders()
    {
        return $this->hasMany(Order::class);
    }

    public function favorites()
    {
        return $this->belongsToMany(Product::class, 'favorites')
            ->withTimestamps();
    }

    public function cart()
    {
        return $this->hasOne(Cart::class);
    }

    public function notifications()
    {
        return $this->hasMany(Notification::class);
    }

    public function addresses()
    {
        return $this->hasMany(UserAddress::class);
    }

    // ─── Scopes ──────────────────────────────────────────────

    public function scopeActive($query)
    {
        return $query->where('phone', 'not like', '%-ي');
    }

    public function scopeVerified($query)
    {
        return $query->where('is_verified', true);
    }
}
