<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Expense extends Model
{
    protected $fillable = [
        'expense_category_id',
        'admin_id',
        'title',
        'amount',
        'spent_on',
        'payment_method',
        'reference',
        'notes',
        'is_recurring',
    ];

    protected function casts(): array
    {
        return [
            'amount' => 'decimal:3',
            'spent_on' => 'date',
            'is_recurring' => 'boolean',
        ];
    }

    public function category()
    {
        return $this->belongsTo(ExpenseCategory::class, 'expense_category_id');
    }

    public function admin()
    {
        return $this->belongsTo(Admin::class);
    }

    public function scopeBetween($query, $from, $to)
    {
        return $query->whereBetween('spent_on', [$from, $to]);
    }

    public function getPaymentMethodLabelAttribute(): string
    {
        return match ($this->payment_method) {
            'cliq' => 'كليك',
            'card' => 'بطاقة',
            'bank' => 'حوالة بنكية',
            default => 'نقداً',
        };
    }
}
