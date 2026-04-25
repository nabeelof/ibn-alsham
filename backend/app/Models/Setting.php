<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Setting extends Model
{
    protected $fillable = ['key', 'value_ar', 'value_en', 'group'];

    public function getValueAttribute(): ?string
    {
        return app()->getLocale() === 'ar' ? $this->value_ar : $this->value_en;
    }

    public static function getValue(string $key, ?string $locale = null): ?string
    {
        $setting = static::where('key', $key)->first();
        if (!$setting) return null;

        $locale = $locale ?? app()->getLocale();
        return $locale === 'ar' ? $setting->value_ar : $setting->value_en;
    }

    public static function getByGroup(string $group): \Illuminate\Database\Eloquent\Collection
    {
        return static::where('group', $group)->get();
    }
}
