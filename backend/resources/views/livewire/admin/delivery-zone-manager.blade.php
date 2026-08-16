@php $money = fn ($v) => number_format((float) $v, 3) . ' د.أ'; @endphp

<div>
    <div class="flex items-center justify-between mb-6">
        <p class="text-sm text-slate-500">
            رسوم التوصيل تُحتسب حسب المسافة بين المطعم وموقع الزبون.
        </p>
        <button wire:click="openForm"
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition">
            + منطقة جديدة
        </button>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table class="w-full text-right">
            <thead class="bg-slate-50 text-slate-500 text-xs">
                <tr>
                    <th class="px-5 py-3 font-bold">المنطقة</th>
                    <th class="px-5 py-3 font-bold">المسافة</th>
                    <th class="px-5 py-3 font-bold">السعر الأساسي</th>
                    <th class="px-5 py-3 font-bold">سعر الكيلومتر</th>
                    <th class="px-5 py-3 font-bold">الحالة</th>
                    <th class="px-5 py-3 font-bold">إجراءات</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($zones as $zone)
                    <tr class="hover:bg-slate-50">
                        <td class="px-5 py-3">
                            <p class="text-sm font-bold text-slate-800">{{ $zone->name_ar }}</p>
                            <p class="text-[11px] text-slate-400" dir="ltr">{{ $zone->name_en }}</p>
                        </td>
                        <td class="px-5 py-3 text-sm text-slate-600">
                            {{ rtrim(rtrim(number_format($zone->min_km, 2), '0'), '.') }} —
                            {{ rtrim(rtrim(number_format($zone->max_km, 2), '0'), '.') }} كم
                        </td>
                        <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($zone->base_price) }}</td>
                        <td class="px-5 py-3 text-sm text-slate-600">{{ $money($zone->price_per_km) }}</td>
                        <td class="px-5 py-3">
                            <span class="px-2.5 py-1 text-[11px] font-bold rounded-full
                                {{ $zone->is_active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600' }}">
                                {{ $zone->is_active ? 'مفعّلة' : 'موقوفة' }}
                            </span>
                        </td>
                        <td class="px-5 py-3 whitespace-nowrap">
                            <button wire:click="openForm({{ $zone->id }})" class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                            <button wire:click="delete({{ $zone->id }})" wire:confirm="حذف هذه المنطقة؟"
                                    class="mr-3 text-xs font-bold text-red-600 hover:underline">حذف</button>
                        </td>
                    </tr>
                @empty
                    <tr><td colspan="6" class="px-5 py-14 text-center text-sm text-slate-400">لا توجد مناطق توصيل</td></tr>
                @endforelse
            </tbody>
        </table>
    </div>

    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-lg">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingId ? 'تعديل منطقة' : 'منطقة توصيل جديدة' }}</h3>
                    <button wire:click="$set('showForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="save" class="p-6 space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالعربية</label>
                            <input type="text" wire:model="nameAr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('nameAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالإنجليزية</label>
                            <input type="text" wire:model="nameEn" dir="ltr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('nameEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">من (كم)</label>
                            <input type="number" step="0.01" wire:model="minKm" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('minKm') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">إلى (كم)</label>
                            <input type="number" step="0.01" wire:model="maxKm" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('maxKm') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">السعر الأساسي (د.أ)</label>
                            <input type="number" step="0.001" wire:model="basePrice" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('basePrice') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">سعر كل كيلومتر إضافي</label>
                            <input type="number" step="0.001" wire:model="pricePerKm" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('pricePerKm') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="isActive" class="rounded text-red-700">
                        المنطقة مفعّلة
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
