<?php

namespace App\Livewire\Admin;

use App\Models\Expense;
use App\Models\ExpenseCategory;
use App\Support\Permissions;
use Livewire\WithPagination;

class ExpenseManager extends AdminPage
{
    use WithPagination;

    protected string $permission = Permissions::EXPENSES_MANAGE;

    public string $search = '';
    public string $categoryFilter = '';
    public string $monthFilter = '';

    // Expense form
    public ?int $editingId = null;
    public bool $showForm = false;
    public string $title = '';
    public string $amount = '';
    public string $spentOn = '';
    public ?int $expenseCategoryId = null;
    public string $paymentMethod = 'cash';
    public string $reference = '';
    public string $notes = '';
    public bool $isRecurring = false;

    // Category form
    public bool $showCategoryForm = false;
    public string $categoryNameAr = '';
    public string $categoryNameEn = '';
    public string $categoryColor = '#6B7280';

    protected function initPage(): void
    {
        $this->spentOn = now()->toDateString();
        $this->monthFilter = now()->format('Y-m');
    }

    protected function rules(): array
    {
        return [
            'title' => 'required|string|max:200',
            'amount' => 'required|numeric|min:0.001',
            'spentOn' => 'required|date',
            'expenseCategoryId' => 'nullable|exists:expense_categories,id',
            'paymentMethod' => 'required|in:cash,cliq,card,bank',
            'reference' => 'nullable|string|max:120',
            'notes' => 'nullable|string|max:1000',
        ];
    }

    protected array $validationAttributes = [
        'title' => 'البيان',
        'amount' => 'المبلغ',
        'spentOn' => 'التاريخ',
        'expenseCategoryId' => 'البند',
        'paymentMethod' => 'طريقة الدفع',
    ];

    public function updatingSearch(): void
    {
        $this->resetPage();
    }

    public function openForm(?int $id = null): void
    {
        $this->resetValidation();
        $this->editingId = $id;

        if ($id) {
            $expense = Expense::findOrFail($id);
            $this->title = $expense->title;
            $this->amount = (string) $expense->amount;
            $this->spentOn = $expense->spent_on->toDateString();
            $this->expenseCategoryId = $expense->expense_category_id;
            $this->paymentMethod = $expense->payment_method;
            $this->reference = $expense->reference ?? '';
            $this->notes = $expense->notes ?? '';
            $this->isRecurring = $expense->is_recurring;
        } else {
            $this->title = '';
            $this->amount = '';
            $this->spentOn = now()->toDateString();
            $this->expenseCategoryId = null;
            $this->paymentMethod = 'cash';
            $this->reference = '';
            $this->notes = '';
            $this->isRecurring = false;
        }

        $this->showForm = true;
    }

    public function save(): void
    {
        $data = $this->validate();

        Expense::updateOrCreate(
            ['id' => $this->editingId],
            [
                'title' => $data['title'],
                'amount' => $data['amount'],
                'spent_on' => $data['spentOn'],
                'expense_category_id' => $data['expenseCategoryId'],
                'payment_method' => $data['paymentMethod'],
                'reference' => $data['reference'] ?: null,
                'notes' => $data['notes'] ?: null,
                'is_recurring' => $this->isRecurring,
                'admin_id' => auth('admin')->id(),
            ]
        );

        $this->showForm = false;
        session()->flash('message', $this->editingId ? 'تم تحديث المصروف' : 'تم تسجيل المصروف');
        $this->editingId = null;
    }

    public function delete(int $id): void
    {
        Expense::findOrFail($id)->delete();
        session()->flash('message', 'تم حذف المصروف');
    }

    public function saveCategory(): void
    {
        $this->validate([
            'categoryNameAr' => 'required|string|max:100',
            'categoryNameEn' => 'required|string|max:100',
            'categoryColor' => 'required|string|max:16',
        ], [], ['categoryNameAr' => 'الاسم بالعربية', 'categoryNameEn' => 'الاسم بالإنجليزية']);

        ExpenseCategory::create([
            'name_ar' => $this->categoryNameAr,
            'name_en' => $this->categoryNameEn,
            'color' => $this->categoryColor,
        ]);

        $this->reset(['categoryNameAr', 'categoryNameEn']);
        $this->categoryColor = '#6B7280';
        $this->showCategoryForm = false;
        session()->flash('message', 'تمت إضافة البند');
    }

    public function render()
    {
        $query = Expense::with(['category', 'admin'])
            ->when($this->search, fn ($q) => $q->where('title', 'like', "%{$this->search}%"))
            ->when($this->categoryFilter, fn ($q) => $q->where('expense_category_id', $this->categoryFilter))
            ->when($this->monthFilter, function ($q) {
                [$year, $month] = explode('-', $this->monthFilter);
                $q->whereYear('spent_on', $year)->whereMonth('spent_on', $month);
            })
            ->latest('spent_on');

        return view('livewire.admin.expense-manager', [
            'expenses' => $query->paginate(15),
            'categories' => ExpenseCategory::active()->orderBy('name_ar')->get(),
            'monthTotal' => (clone $query)->sum('amount'),
        ])->title('المصروفات');
    }
}
