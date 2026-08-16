<div>
    @if(session('message'))
        <div class="mb-4 p-4 bg-green-100 border border-green-300 text-green-800 rounded-lg font-semibold">
            {{ session('message') }}
        </div>
    @endif

    <!-- Toolbar -->
    <div class="flex flex-col md:flex-row gap-4 mb-6 items-center">
        <input
            wire:model.live.debounce.300ms="search"
            type="text"
            placeholder="بحث في الأقسام..."
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none"
        >
        <button wire:click="showCreateForm" class="px-6 py-2 bg-red-700 text-white font-bold rounded-lg hover:bg-red-800 transition whitespace-nowrap">
            + إضافة قسم
        </button>
    </div>

    <!-- Categories Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-6">
        @forelse($categories as $category)
            <div class="bg-white rounded-xl shadow border border-gray-100 overflow-hidden flex flex-col">
                <div class="h-40 bg-gray-100 flex items-center justify-center overflow-hidden">
                    @if($category->image)
                        <img src="{{ Storage::url($category->image) }}" alt="{{ $category->name_ar }}" class="w-full h-full object-cover">
                    @else
                        <span class="text-gray-400 text-4xl">🗂️</span>
                    @endif
                </div>

                <div class="p-4 flex flex-col flex-1">
                    <h3 class="font-bold text-gray-800">{{ $category->name_ar }}</h3>
                    <p class="text-sm text-gray-500" dir="ltr">{{ $category->name_en }}</p>

                    <div class="flex items-center gap-2 mt-2 text-xs text-gray-500">
                        <span>الترتيب: {{ $category->sort_order }}</span>
                        <span>•</span>
                        <span>{{ $category->products_count }} صنف</span>
                    </div>

                    <div class="mt-auto pt-4 flex gap-2">
                        <button wire:click="toggleActive({{ $category->id }})"
                            class="flex-1 py-1.5 text-sm font-bold rounded-lg transition {{ $category->is_active ? 'bg-green-100 text-green-700 hover:bg-green-200' : 'bg-gray-100 text-gray-500 hover:bg-gray-200' }}">
                            {{ $category->is_active ? '✓ نشط' : '✗ غير نشط' }}
                        </button>

                        <button wire:click="showEditForm({{ $category->id }})"
                            class="px-3 py-1.5 text-sm font-bold rounded-lg bg-blue-100 text-blue-700 hover:bg-blue-200 transition">
                            تعديل
                        </button>

                        <button wire:click="deleteCategory({{ $category->id }})"
                            wire:confirm="هل أنت متأكد من حذف هذا القسم؟"
                            class="px-3 py-1.5 text-sm font-bold rounded-lg bg-red-100 text-red-700 hover:bg-red-200 transition">
                            حذف
                        </button>
                    </div>
                </div>
            </div>
        @empty
            <div class="col-span-full text-center py-16 text-gray-400">
                لا يوجد أقسام مطابقة.
            </div>
        @endforelse
    </div>

    <div>{{ $categories->links() }}</div>

    <!-- Create / Edit Form Modal -->
    @if($formMode)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60" wire:click.self="closeForm">
            <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4">
                <div class="flex items-center justify-between p-6 border-b">
                    <h3 class="text-xl font-bold text-gray-800">
                        {{ $formMode === 'create' ? 'إضافة قسم جديد' : 'تعديل القسم' }}
                    </h3>
                    <button wire:click="closeForm" class="text-gray-400 hover:text-gray-600 text-2xl leading-none">&times;</button>
                </div>

                <form wire:submit="save" class="p-6 space-y-5">
                    <div class="grid grid-cols-2 gap-5">
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">الاسم بالعربية *</label>
                            <input wire:model="nameAr" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
                            @error('nameAr') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">الاسم بالإنجليزية *</label>
                            <input wire:model="nameEn" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none" dir="ltr">
                            @error('nameEn') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-5 items-center">
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">ترتيب العرض</label>
                            <input wire:model="sortOrder" type="number" min="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
                            @error('sortOrder') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                        </div>
                        <div class="flex items-center gap-3 mt-5">
                            <input wire:model="isActive" type="checkbox" id="isActive" class="w-5 h-5 text-red-600 rounded focus:ring-red-500">
                            <label for="isActive" class="text-sm font-semibold text-gray-700">نشط</label>
                        </div>
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-gray-700 mb-1">صورة القسم</label>
                        @if($formMode === 'edit' && $editingCategoryId)
                            @php $existing = \App\Models\Category::find($editingCategoryId); @endphp
                            @if($existing && $existing->image)
                                <div class="mb-2">
                                    <img src="{{ Storage::url($existing->image) }}" alt="" class="w-24 h-24 object-cover rounded-lg border">
                                </div>
                            @endif
                        @endif
                        <input wire:model="image" type="file" accept="image/*" class="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:bg-red-100 file:text-red-700 file:font-semibold hover:file:bg-red-200">
                        @error('image') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                    </div>

                    <div class="flex justify-end gap-3 pt-4 border-t">
                        <button type="button" wire:click="closeForm" class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-semibold">
                            إلغاء
                        </button>
                        <button type="submit" class="px-8 py-2 bg-red-700 text-white rounded-lg hover:bg-red-800 font-bold transition">
                            {{ $formMode === 'create' ? 'إضافة القسم' : 'حفظ التعديلات' }}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    @endif
</div>
