<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ExpenseCategory extends Model
{
    protected $fillable = ['name_ar', 'name_en', 'color', 'is_active'];

    protected function casts(): array
    {
        return ['is_active' => 'boolean'];
    }

    public function expenses()
    {
        return $this->hasMany(Expense::class);
    }

    public function scopeActive($query)
    {
        return $query->where('is_active', true);
    }

    public function getNameAttribute(): string
    {
        return app()->getLocale() === 'ar' ? $this->name_ar : $this->name_en;
    }
}
