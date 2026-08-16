<div>
    <div class="flex items-center justify-between mb-6">
        <p class="text-sm text-slate-500">
            حدّد ما يستطيع كل دور الوصول إليه. المشرف يرى فقط الصفحات المسموح بها في دوره.
        </p>
        <button wire:click="openForm"
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition">
            + دور جديد
        </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        @foreach($roles as $role)
            <div class="bg-white rounded-xl border border-slate-200 p-5">
                <div class="flex items-start justify-between">
                    <div>
                        <h3 class="font-extrabold text-slate-900">{{ $role->name_ar }}</h3>
                        <p class="text-xs text-slate-400 mt-0.5" dir="ltr">{{ $role->name_en }}</p>
                    </div>
                    @if($role->is_system)
                        <span class="px-2 py-1 text-[10px] font-bold text-blue-700 bg-blue-50 rounded-full">أساسي</span>
                    @endif
                </div>

                @if($role->description_ar)
                    <p class="mt-2 text-xs text-slate-500">{{ $role->description_ar }}</p>
                @endif

                <div class="mt-4 flex items-center gap-4 text-xs text-slate-500">
                    <span><span class="font-bold text-slate-800">{{ $role->admins_count }}</span> مشرف</span>
                    <span>
                        <span class="font-bold text-slate-800">
                            {{ $role->key === 'super_admin' ? 'كل' : $role->permissions_count }}
                        </span> صلاحية
                    </span>
                </div>

                <div class="mt-4 flex gap-3 pt-3 border-t border-slate-100">
                    <button wire:click="openForm({{ $role->id }})"
                            class="text-xs font-bold text-blue-600 hover:underline">
                        {{ $role->key === 'super_admin' ? 'عرض' : 'تعديل الصلاحيات' }}
                    </button>
                    @unless($role->is_system)
                        <button wire:click="delete({{ $role->id }})"
                                wire:confirm="هل تريد حذف هذا الدور؟"
                                class="text-xs font-bold text-red-600 hover:underline">حذف</button>
                    @endunless
                </div>
            </div>
        @endforeach
    </div>

    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-3xl max-h-[92vh] overflow-y-auto">
                <div class="sticky top-0 bg-white px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingId ? 'تعديل الدور' : 'دور جديد' }}</h3>
                    <button wire:click="$set('showForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>

                <form wire:submit="save" class="p-6 space-y-5">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">اسم الدور بالعربية</label>
                            <input type="text" wire:model="nameAr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('nameAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">الاسم بالإنجليزية</label>
                            <input type="text" wire:model="nameEn" dir="ltr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('nameEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">وصف مختصر</label>
                        <input type="text" wire:model="descriptionAr"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    </div>

                    <div>
                        <p class="text-xs font-bold text-slate-600 mb-3">الصلاحيات</p>

                        <div class="space-y-4">
                            @foreach($groups as $groupName => $items)
                                <div class="border border-slate-200 rounded-lg overflow-hidden">
                                    <div class="flex items-center justify-between px-4 py-2.5 bg-slate-50">
                                        <span class="text-sm font-bold text-slate-700">{{ $groupName }}</span>
                                        <button type="button" wire:click="toggleGroup('{{ $groupName }}')"
                                                class="text-[11px] font-bold text-red-700 hover:underline">
                                            تحديد الكل
                                        </button>
                                    </div>
                                    <div class="p-4 grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                                        @foreach($items as $key => $label)
                                            <label class="flex items-center gap-2 text-sm text-slate-700">
                                                <input type="checkbox" value="{{ $key }}"
                                                       wire:model="selectedPermissions"
                                                       class="rounded text-red-700 focus:ring-red-500">
                                                {{ $label }}
                                            </label>
                                        @endforeach
                                    </div>
                                </div>
                            @endforeach
                        </div>
                    </div>

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
</div>
