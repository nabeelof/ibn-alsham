@php $money = fn ($v) => number_format((float) $v, 3) . ' د.أ'; @endphp

<div>
    <!-- Toolbar -->
    <div class="bg-white rounded-xl border border-slate-200 p-4 mb-6 flex flex-wrap items-end gap-3">
        <div class="flex-1 min-w-[180px]">
            <label class="block text-[11px] font-bold text-slate-500 mb-1">بحث</label>
            <input type="text" wire:model.live.debounce.400ms="search" placeholder="ابحث في البيان…"
                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
        </div>
        <div>
            <label class="block text-[11px] font-bold text-slate-500 mb-1">البند</label>
            <select wire:model.live="categoryFilter" class="px-3 py-2 text-sm border border-slate-300 rounded-lg">
                <option value="">كل البنود</option>
                @foreach($categories as $category)
                    <option value="{{ $category->id }}">{{ $category->name_ar }}</option>
                @endforeach
            </select>
        </div>
        <div>
            <label class="block text-[11px] font-bold text-slate-500 mb-1">الشهر</label>
            <input type="month" wire:model.live="monthFilter"
                   class="px-3 py-2 text-sm border border-slate-300 rounded-lg">
        </div>
        <button wire:click="openForm"
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition">
            + تسجيل مصروف
        </button>
        <button wire:click="$set('showCategoryForm', true)"
                class="px-4 py-2 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg hover:bg-slate-200 transition">
            + بند جديد
        </button>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <x-admin.stat label="إجمالي المصروفات (حسب الفلتر)" :value="$money($monthTotal)" tone="red" icon="wallet" />
        <x-admin.stat label="عدد القيود" :value="$expenses->total()" tone="slate" />
        <x-admin.stat label="عدد البنود" :value="$categories->count()" tone="blue" />
    </div>

    <!-- Table -->
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table class="w-full text-right">
            <thead class="bg-slate-50 text-slate-500 text-xs">
                <tr>
                    <th class="px-5 py-3 font-bold">التاريخ</th>
                    <th class="px-5 py-3 font-bold">البيان</th>
                    <th class="px-5 py-3 font-bold">البند</th>
                    <th class="px-5 py-3 font-bold">طريقة الدفع</th>
                    <th class="px-5 py-3 font-bold">المبلغ</th>
                    <th class="px-5 py-3 font-bold">إجراءات</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($expenses as $expense)
                    <tr class="hover:bg-slate-50">
                        <td class="px-5 py-3 text-sm text-slate-600">{{ $expense->spent_on->format('Y-m-d') }}</td>
                        <td class="px-5 py-3">
                            <p class="text-sm font-semibold text-slate-800">{{ $expense->title }}</p>
                            @if($expense->reference)
                                <p class="text-[11px] text-slate-400">مرجع: {{ $expense->reference }}</p>
                            @endif
                        </td>
                        <td class="px-5 py-3">
                            @if($expense->category)
                                <span class="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600">
                                    <span class="w-2 h-2 rounded-full" style="background: {{ $expense->category->color }}"></span>
                                    {{ $expense->category->name_ar }}
                                </span>
                            @else
                                <span class="text-xs text-slate-400">غير مصنّف</span>
                            @endif
                        </td>
                        <td class="px-5 py-3 text-sm text-slate-600">{{ $expense->payment_method_label }}</td>
                        <td class="px-5 py-3 text-sm font-bold text-red-600">{{ $money($expense->amount) }}</td>
                        <td class="px-5 py-3 whitespace-nowrap">
                            <button wire:click="openForm({{ $expense->id }})"
                                    class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                            <button wire:click="delete({{ $expense->id }})"
                                    wire:confirm="هل تريد حذف هذا المصروف؟"
                                    class="mr-3 text-xs font-bold text-red-600 hover:underline">حذف</button>
                        </td>
                    </tr>
                @empty
                    <tr><td colspan="6" class="px-5 py-14 text-center text-sm text-slate-400">لا توجد مصروفات مسجّلة</td></tr>
                @endforelse
            </tbody>
        </table>
        <div class="px-5 py-3 border-t border-slate-100">{{ $expenses->links() }}</div>
    </div>

    <!-- Expense modal -->
    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingId ? 'تعديل مصروف' : 'تسجيل مصروف' }}</h3>
                    <button wire:click="$set('showForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="save" class="p-6 space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">البيان</label>
                        <input type="text" wire:model="title"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                        @error('title') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">المبلغ (د.أ)</label>
                            <input type="number" step="0.001" wire:model="amount"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('amount') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">التاريخ</label>
                            <input type="date" wire:model="spentOn"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('spentOn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">البند</label>
                            <select wire:model="expenseCategoryId"
                                    class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                                <option value="">غير مصنّف</option>
                                @foreach($categories as $category)
                                    <option value="{{ $category->id }}">{{ $category->name_ar }}</option>
                                @endforeach
                            </select>
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">طريقة الدفع</label>
                            <select wire:model="paymentMethod"
                                    class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                                <option value="cash">نقداً</option>
                                <option value="cliq">كليك</option>
                                <option value="card">بطاقة</option>
                                <option value="bank">حوالة بنكية</option>
                            </select>
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">رقم المرجع / الفاتورة (اختياري)</label>
                        <input type="text" wire:model="reference"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">ملاحظات</label>
                        <textarea wire:model="notes" rows="2"
                                  class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg"></textarea>
                    </div>

                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="isRecurring" class="rounded text-red-700">
                        مصروف متكرّر شهرياً
                    </label>

                    <div class="flex gap-3 pt-2">
                        <button type="submit"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800">حفظ</button>
                        <button type="button" wire:click="$set('showForm', false)"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg hover:bg-slate-200">إلغاء</button>
                    </div>
                </form>
            </div>
        </div>
    @endif

    <!-- Category modal -->
    @if($showCategoryForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-md">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">بند مصروفات جديد</h3>
                    <button wire:click="$set('showCategoryForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="saveCategory" class="p-6 space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالعربية</label>
                        <input type="text" wire:model="categoryNameAr"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        @error('categoryNameAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالإنجليزية</label>
                        <input type="text" wire:model="categoryNameEn" dir="ltr"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        @error('categoryNameEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">اللون</label>
                        <input type="color" wire:model="categoryColor" class="h-10 w-20 border border-slate-300 rounded-lg">
                    </div>
                    <div class="flex gap-3 pt-2">
                        <button type="submit" class="flex-1 px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg">حفظ</button>
                        <button type="button" wire:click="$set('showCategoryForm', false)"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg">إلغاء</button>
                    </div>
                </form>
            </div>
        </div>
    @endif
</div>
