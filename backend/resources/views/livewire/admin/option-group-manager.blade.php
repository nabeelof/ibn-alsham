@php $money = fn ($v) => number_format((float) $v, 3) . ' د.أ'; @endphp

<div>
    <div class="bg-white rounded-xl border border-slate-200 p-4 mb-6 flex flex-wrap items-end gap-3">
        <div class="flex-1 min-w-[240px]">
            <label class="block text-[11px] font-bold text-slate-500 mb-1">المنتج</label>
            <select wire:model.live="productId" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                @foreach($products as $product)
                    <option value="{{ $product->id }}">{{ $product->name_ar }}</option>
                @endforeach
            </select>
        </div>
        <button wire:click="openGroupForm" @disabled(!$productId)
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition disabled:opacity-50">
            + مجموعة خيارات
        </button>
    </div>

    <p class="text-sm text-slate-500 mb-5">
        كل مجموعة تحدّد كيف يختار الزبون: «اختيار واحد» تظهر كأزرار دائرية، و«اختيار متعدّد» تظهر كمربّعات.
        الخيار بسعر صفر يظهر للزبون كـ «مجاناً».
    </p>

    <div class="space-y-5">
        @forelse($groups as $group)
            <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
                <div class="px-5 py-4 border-b border-slate-100 bg-slate-50 flex flex-wrap items-center justify-between gap-3">
                    <div>
                        <div class="flex items-center gap-2">
                            <h3 class="font-extrabold text-slate-900">{{ $group->name_ar }}</h3>
                            <span class="px-2 py-0.5 text-[10px] font-bold rounded-full
                                {{ $group->is_required ? 'bg-red-100 text-red-700' : 'bg-slate-200 text-slate-600' }}">
                                {{ $group->is_required ? 'إلزامي' : 'اختياري' }}
                            </span>
                            <span class="px-2 py-0.5 text-[10px] font-bold rounded-full bg-blue-50 text-blue-700">
                                {{ $group->type === 'radio' ? 'اختيار واحد' : 'اختيار متعدّد' }}
                            </span>
                        </div>
                        <p class="text-[11px] text-slate-400 mt-1">
                            الحد الأدنى {{ $group->min_selections }}
                            @if($group->max_selections) · الحد الأقصى {{ $group->max_selections }} @endif
                            · {{ $group->options->count() }} خيار
                        </p>
                    </div>
                    <div class="flex gap-3">
                        <button wire:click="openOptionForm({{ $group->id }})"
                                class="text-xs font-bold text-emerald-600 hover:underline">+ خيار</button>
                        <button wire:click="openGroupForm({{ $group->id }})"
                                class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                        <button wire:click="deleteGroup({{ $group->id }})"
                                wire:confirm="سيتم حذف المجموعة وجميع خياراتها. متابعة؟"
                                class="text-xs font-bold text-red-600 hover:underline">حذف</button>
                    </div>
                </div>

                <table class="w-full text-right">
                    <tbody class="divide-y divide-slate-100">
                        @forelse($group->options as $option)
                            <tr class="hover:bg-slate-50">
                                <td class="px-5 py-3">
                                    <p class="text-sm font-semibold text-slate-800">{{ $option->name_ar }}</p>
                                    <p class="text-[11px] text-slate-400" dir="ltr">{{ $option->name_en }}</p>
                                </td>
                                <td class="px-5 py-3 text-sm font-bold {{ $option->price > 0 ? 'text-red-600' : 'text-emerald-600' }}">
                                    {{ $option->price > 0 ? '+' . $money($option->price) : 'مجاناً' }}
                                </td>
                                <td class="px-5 py-3 text-[11px] text-slate-500">
                                    @if($option->default_selected) <span class="font-bold text-blue-600">محدّد مسبقاً</span> @endif
                                    @unless($option->is_user_changeable) <span class="font-bold text-amber-600">غير قابل للتغيير</span> @endunless
                                </td>
                                <td class="px-5 py-3 whitespace-nowrap">
                                    <button wire:click="openOptionForm({{ $group->id }}, {{ $option->id }})"
                                            class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                                    <button wire:click="deleteOption({{ $option->id }})" wire:confirm="حذف هذا الخيار؟"
                                            class="mr-3 text-xs font-bold text-red-600 hover:underline">حذف</button>
                                </td>
                            </tr>
                        @empty
                            <tr><td colspan="4" class="px-5 py-8 text-center text-sm text-slate-400">لا توجد خيارات — أضِف أول خيار</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        @empty
            <div class="bg-white rounded-xl border border-slate-200 py-16 text-center text-sm text-slate-400">
                لا توجد مجموعات خيارات لهذا المنتج
            </div>
        @endforelse
    </div>

    <!-- Group modal -->
    @if($showGroupForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-lg">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingGroupId ? 'تعديل مجموعة' : 'مجموعة خيارات جديدة' }}</h3>
                    <button wire:click="$set('showGroupForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="saveGroup" class="p-6 space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالعربية</label>
                            <input type="text" wire:model="groupNameAr" placeholder="مثال: حجم الوجبة"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('groupNameAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالإنجليزية</label>
                            <input type="text" wire:model="groupNameEn" dir="ltr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('groupNameEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">طريقة الاختيار</label>
                        <select wire:model.live="groupType" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            <option value="checkbox">اختيار متعدّد (مربّعات)</option>
                            <option value="radio">اختيار واحد (أزرار دائرية)</option>
                        </select>
                    </div>

                    @if($groupType === 'checkbox')
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1">أقل عدد اختيارات</label>
                                <input type="number" wire:model="groupMin" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1">أكثر عدد اختيارات</label>
                                <input type="number" wire:model="groupMax" placeholder="غير محدود"
                                       class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            </div>
                        </div>
                    @endif

                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="groupRequired" class="rounded text-red-700">
                        إلزامي — لا يستطيع الزبون إضافة المنتج للسلة بدون اختيار
                    </label>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الترتيب</label>
                        <input type="number" wire:model="groupSort" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    </div>

                    <div class="flex gap-3 pt-2">
                        <button type="submit" class="flex-1 px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800">حفظ</button>
                        <button type="button" wire:click="$set('showGroupForm', false)"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg hover:bg-slate-200">إلغاء</button>
                    </div>
                </form>
            </div>
        </div>
    @endif

    <!-- Option modal -->
    @if($showOptionForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-md">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingOptionId ? 'تعديل خيار' : 'خيار جديد' }}</h3>
                    <button wire:click="$set('showOptionForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="saveOption" class="p-6 space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالعربية</label>
                        <input type="text" wire:model="optionNameAr" placeholder="مثال: إضافة جبنة"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        @error('optionNameAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالإنجليزية</label>
                        <input type="text" wire:model="optionNameEn" dir="ltr"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        @error('optionNameEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">السعر الإضافي (د.أ)</label>
                        <input type="number" step="0.001" wire:model="optionPrice"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        <p class="mt-1 text-[11px] text-slate-400">اتركه صفراً ليظهر للزبون كإضافة مجانية.</p>
                        @error('optionPrice') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>

                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="optionDefault" class="rounded text-red-700">
                        محدّد مسبقاً عند فتح المنتج
                    </label>
                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="optionChangeable" class="rounded text-red-700">
                        يستطيع الزبون تغييره
                    </label>

                    <div class="flex gap-3 pt-2">
                        <button type="submit" class="flex-1 px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800">حفظ</button>
                        <button type="button" wire:click="$set('showOptionForm', false)"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg hover:bg-slate-200">إلغاء</button>
                    </div>
                </form>
            </div>
        </div>
    @endif
</div>
