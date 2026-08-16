@php $money = fn ($v) => number_format((float) $v, 3) . ' د.أ'; @endphp

<div>
    <div class="bg-white rounded-xl border border-slate-200 p-4 mb-6 flex flex-wrap items-end gap-3">
        <div class="flex-1 min-w-[200px]">
            <label class="block text-[11px] font-bold text-slate-500 mb-1">بحث بالكود</label>
            <input type="text" wire:model.live.debounce.400ms="search" placeholder="WELCOME10…" dir="ltr"
                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
        </div>
        <button wire:click="openForm"
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition">
            + كوبون جديد
        </button>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table class="w-full text-right">
            <thead class="bg-slate-50 text-slate-500 text-xs">
                <tr>
                    <th class="px-5 py-3 font-bold">الكود</th>
                    <th class="px-5 py-3 font-bold">الوصف</th>
                    <th class="px-5 py-3 font-bold">الخصم</th>
                    <th class="px-5 py-3 font-bold">الحد الأدنى</th>
                    <th class="px-5 py-3 font-bold">الاستخدام</th>
                    <th class="px-5 py-3 font-bold">الصلاحية</th>
                    <th class="px-5 py-3 font-bold">الحالة</th>
                    <th class="px-5 py-3 font-bold">إجراءات</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($coupons as $coupon)
                    <tr class="hover:bg-slate-50">
                        <td class="px-5 py-3 text-sm font-extrabold text-slate-800" dir="ltr">{{ $coupon->code }}</td>
                        <td class="px-5 py-3 text-sm text-slate-600">{{ $coupon->title_ar }}</td>
                        <td class="px-5 py-3 text-sm font-bold text-emerald-600">
                            @if($coupon->discount_type === 'percent')
                                {{ rtrim(rtrim(number_format($coupon->discount_value, 2), '0'), '.') }}%
                                @if($coupon->max_discount)
                                    <span class="block text-[10px] font-normal text-slate-400">حد أقصى {{ $money($coupon->max_discount) }}</span>
                                @endif
                            @else
                                {{ $money($coupon->discount_value) }}
                            @endif
                        </td>
                        <td class="px-5 py-3 text-sm text-slate-600">{{ $money($coupon->min_order_total) }}</td>
                        <td class="px-5 py-3 text-sm text-slate-600">
                            {{ $coupon->used_count }}{{ $coupon->usage_limit ? ' / ' . $coupon->usage_limit : '' }}
                        </td>
                        <td class="px-5 py-3 text-xs text-slate-500">
                            {{ $coupon->expires_at?->format('Y-m-d') ?? 'بدون انتهاء' }}
                        </td>
                        <td class="px-5 py-3">
                            <span class="px-2.5 py-1 text-[11px] font-bold rounded-full
                                {{ $coupon->is_active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600' }}">
                                {{ $coupon->is_active ? 'مفعّل' : 'موقوف' }}
                            </span>
                        </td>
                        <td class="px-5 py-3 whitespace-nowrap">
                            <button wire:click="openForm({{ $coupon->id }})" class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                            <button wire:click="toggleActive({{ $coupon->id }})" class="mr-3 text-xs font-bold text-amber-600 hover:underline">
                                {{ $coupon->is_active ? 'إيقاف' : 'تفعيل' }}
                            </button>
                            <button wire:click="delete({{ $coupon->id }})" wire:confirm="هل تريد حذف هذا الكوبون؟"
                                    class="mr-3 text-xs font-bold text-red-600 hover:underline">حذف</button>
                        </td>
                    </tr>
                @empty
                    <tr><td colspan="8" class="px-5 py-14 text-center text-sm text-slate-400">لا توجد كوبونات</td></tr>
                @endforelse
            </tbody>
        </table>
        <div class="px-5 py-3 border-t border-slate-100">{{ $coupons->links() }}</div>
    </div>

    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-2xl max-h-[92vh] overflow-y-auto">
                <div class="sticky top-0 bg-white px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingId ? 'تعديل كوبون' : 'كوبون جديد' }}</h3>
                    <button wire:click="$set('showForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="save" class="p-6 space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">كود الخصم</label>
                            <input type="text" wire:model="code" dir="ltr" placeholder="WELCOME10"
                                   class="w-full px-3 py-2 text-sm uppercase border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('code') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">نوع الخصم</label>
                            <select wire:model.live="discountType" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                                <option value="percent">نسبة مئوية (%)</option>
                                <option value="fixed">مبلغ ثابت (د.أ)</option>
                            </select>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الوصف بالعربية</label>
                            <input type="text" wire:model="titleAr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('titleAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الوصف بالإنجليزية</label>
                            <input type="text" wire:model="titleEn" dir="ltr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('titleEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-3 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">
                                {{ $discountType === 'percent' ? 'النسبة (%)' : 'المبلغ (د.أ)' }}
                            </label>
                            <input type="number" step="0.001" wire:model="discountValue"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('discountValue') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">حد أقصى للخصم</label>
                            <input type="number" step="0.001" wire:model="maxDiscount" placeholder="اختياري"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg"
                                   @disabled($discountType !== 'percent')>
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الحد الأدنى للطلب</label>
                            <input type="number" step="0.001" wire:model="minOrderTotal"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('minOrderTotal') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">عدد مرات الاستخدام الكلي</label>
                            <input type="number" wire:model="usageLimit" placeholder="غير محدود"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">مرات الاستخدام لكل زبون</label>
                            <input type="number" wire:model="usageLimitPerUser" placeholder="غير محدود"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">يبدأ في</label>
                            <input type="date" wire:model="startsAt"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">ينتهي في</label>
                            <input type="date" wire:model="expiresAt"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('expiresAt') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="isActive" class="rounded text-red-700">
                        الكوبون مفعّل
                    </label>

                    <div class="flex gap-3 pt-2">
                        <button type="submit" class="flex-1 px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800">حفظ</button>
                        <button type="button" wire:click="$set('showForm', false)"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg hover:bg-slate-200">إلغاء</button>
                    </div>
                </form>
            </div>
        </div>
    @endif
</div>
