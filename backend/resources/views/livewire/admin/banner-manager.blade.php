<div>
    <div class="flex items-center justify-between mb-6">
        <p class="text-sm text-slate-500">البانرات التي تظهر في أعلى الصفحة الرئيسية للتطبيق.</p>
        <button wire:click="openForm"
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition">
            + بانر جديد
        </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
        @forelse($banners as $banner)
            <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
                <div class="h-36 bg-slate-100">
                    @if($banner->image)
                        <img src="{{ asset('storage/' . $banner->image) }}" alt="{{ $banner->title_ar }}"
                             class="w-full h-full object-cover">
                    @else
                        <div class="w-full h-full flex items-center justify-center text-xs text-slate-400">لا توجد صورة</div>
                    @endif
                </div>
                <div class="p-4">
                    <div class="flex items-start justify-between">
                        <h3 class="font-bold text-slate-800 text-sm">{{ $banner->title_ar }}</h3>
                        <span class="px-2 py-0.5 text-[10px] font-bold rounded-full
                            {{ $banner->is_active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600' }}">
                            {{ $banner->is_active ? 'ظاهر' : 'مخفي' }}
                        </span>
                    </div>
                    <p class="mt-1 text-[11px] text-slate-400">
                        الترتيب {{ $banner->sort_order }} ·
                        {{ ['none' => 'بدون رابط', 'product' => 'منتج', 'category' => 'قسم'][$banner->link_type] ?? 'بدون رابط' }}
                    </p>
                    <div class="mt-3 flex gap-3 pt-3 border-t border-slate-100">
                        <button wire:click="openForm({{ $banner->id }})" class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                        <button wire:click="toggleActive({{ $banner->id }})" class="text-xs font-bold text-amber-600 hover:underline">
                            {{ $banner->is_active ? 'إخفاء' : 'إظهار' }}
                        </button>
                        <button wire:click="delete({{ $banner->id }})" wire:confirm="حذف هذا البانر؟"
                                class="text-xs font-bold text-red-600 hover:underline">حذف</button>
                    </div>
                </div>
            </div>
        @empty
            <div class="col-span-full bg-white rounded-xl border border-slate-200 py-16 text-center text-sm text-slate-400">
                لا توجد بانرات
            </div>
        @endforelse
    </div>

    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-lg max-h-[92vh] overflow-y-auto">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingId ? 'تعديل بانر' : 'بانر جديد' }}</h3>
                    <button wire:click="$set('showForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="save" class="p-6 space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">العنوان بالعربية</label>
                            <input type="text" wire:model="titleAr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('titleAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">العنوان بالإنجليزية</label>
                            <input type="text" wire:model="titleEn" dir="ltr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            @error('titleEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الصورة</label>
                        <input type="file" wire:model="imageFile" accept="image/*"
                               class="w-full text-sm text-slate-600 file:mr-3 file:py-2 file:px-3 file:rounded-lg file:border-0 file:bg-slate-100 file:text-xs file:font-bold">
                        <div wire:loading wire:target="imageFile" class="mt-1 text-[11px] text-slate-400">جارٍ الرفع…</div>
                        @error('imageFile') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        @if($imageFile)
                            <img src="{{ $imageFile->temporaryUrl() }}" class="mt-2 h-24 rounded-lg object-cover">
                        @endif
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">عند الضغط ينتقل إلى</label>
                            <select wire:model.live="linkType" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                                <option value="none">لا شيء</option>
                                <option value="product">منتج</option>
                                <option value="category">قسم</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الهدف</label>
                            <select wire:model="linkValue" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg"
                                    @disabled($linkType === 'none')>
                                <option value="">— اختر —</option>
                                @if($linkType === 'product')
                                    @foreach($products as $product)
                                        <option value="{{ $product->id }}">{{ $product->name_ar }}</option>
                                    @endforeach
                                @elseif($linkType === 'category')
                                    @foreach($categories as $category)
                                        <option value="{{ $category->id }}">{{ $category->name_ar }}</option>
                                    @endforeach
                                @endif
                            </select>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4 items-end">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الترتيب</label>
                            <input type="number" wire:model="sortOrder" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        </div>
                        <label class="flex items-center gap-2 text-sm text-slate-700 pb-2">
                            <input type="checkbox" wire:model="isActive" class="rounded text-red-700">
                            ظاهر في التطبيق
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
