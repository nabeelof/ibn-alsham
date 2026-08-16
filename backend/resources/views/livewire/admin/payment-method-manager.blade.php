<div>
    <p class="text-sm text-slate-500 mb-6">
        وسائل الدفع المتاحة في الأردن. ما يُفعّل هنا هو ما يظهر للزبون في صفحة إتمام الطلب.
    </p>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
        @foreach($methods as $method)
            <div class="bg-white rounded-xl border border-slate-200 p-5">
                <div class="flex items-start justify-between">
                    <div>
                        <h3 class="font-extrabold text-slate-900">{{ $method->name_ar }}</h3>
                        <p class="text-xs text-slate-400 mt-0.5" dir="ltr">{{ $method->name_en }}</p>
                    </div>
                    <span class="px-2.5 py-1 text-[11px] font-bold rounded-full
                        {{ $method->is_active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600' }}">
                        {{ $method->is_active ? 'مفعّلة' : 'موقوفة' }}
                    </span>
                </div>

                @if($method->description_ar)
                    <p class="mt-2 text-xs text-slate-500">{{ $method->description_ar }}</p>
                @endif

                <div class="mt-4 flex flex-wrap gap-1.5">
                    @if($method->account_alias)
                        <span class="px-2 py-1 text-[10px] font-bold text-amber-700 bg-amber-50 rounded">
                            الاسم: {{ $method->account_alias }}
                        </span>
                    @endif
                    @if($method->requires_reference)
                        <span class="px-2 py-1 text-[10px] font-bold text-blue-700 bg-blue-50 rounded">يتطلّب رقم حوالة</span>
                    @endif
                    @if($method->available_for_delivery)
                        <span class="px-2 py-1 text-[10px] font-bold text-slate-600 bg-slate-100 rounded">توصيل</span>
                    @endif
                    @if($method->available_for_pickup)
                        <span class="px-2 py-1 text-[10px] font-bold text-slate-600 bg-slate-100 rounded">استلام</span>
                    @endif
                </div>

                <div class="mt-4 flex gap-3 pt-3 border-t border-slate-100">
                    <button wire:click="openForm({{ $method->id }})" class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                    <button wire:click="toggleActive({{ $method->id }})" class="text-xs font-bold text-amber-600 hover:underline">
                        {{ $method->is_active ? 'إيقاف' : 'تفعيل' }}
                    </button>
                </div>
            </div>
        @endforeach
    </div>

    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-lg max-h-[92vh] overflow-y-auto">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">تعديل وسيلة الدفع</h3>
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

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الوصف بالعربية</label>
                        <input type="text" wire:model="descriptionAr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الوصف بالإنجليزية</label>
                        <input type="text" wire:model="descriptionEn" dir="ltr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">اسم كليك / رقم الحساب</label>
                        <input type="text" wire:model="accountAlias" dir="ltr" placeholder="IBNALSHAM"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        <p class="mt-1 text-[11px] text-slate-400">يظهر للزبون ليحوّل إليه المبلغ.</p>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الترتيب</label>
                        <input type="number" wire:model="sortOrder" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    </div>

                    <div class="space-y-2 pt-1">
                        <label class="flex items-center gap-2 text-sm text-slate-700">
                            <input type="checkbox" wire:model="requiresReference" class="rounded text-red-700">
                            يتطلّب من الزبون إدخال رقم الحوالة
                        </label>
                        <label class="flex items-center gap-2 text-sm text-slate-700">
                            <input type="checkbox" wire:model="availableForDelivery" class="rounded text-red-700">
                            متاحة لطلبات التوصيل
                        </label>
                        <label class="flex items-center gap-2 text-sm text-slate-700">
                            <input type="checkbox" wire:model="availableForPickup" class="rounded text-red-700">
                            متاحة لطلبات الاستلام
                        </label>
                        <label class="flex items-center gap-2 text-sm text-slate-700">
                            <input type="checkbox" wire:model="isActive" class="rounded text-red-700">
                            مفعّلة
                        </label>
                    </div>

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
