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
            placeholder="بحث في الأصناف..."
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none"
        >
        <select wire:model.live="categoryFilter" class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
            <option value="">كل الأقسام</option>
            @foreach($categories as $category)
                <option value="{{ $category->id }}">{{ $category->name_ar }}</option>
            @endforeach
        </select>
        <button wire:click="showCreateForm" class="px-6 py-2 bg-red-700 text-white font-bold rounded-lg hover:bg-red-800 transition whitespace-nowrap">
            + إضافة صنف
        </button>
    </div>

    <!-- Product Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-6">
        @forelse($products as $product)
            <div class="bg-white rounded-xl shadow border border-gray-100 overflow-hidden flex flex-col">
                <!-- Product Image -->
                <div class="h-40 bg-gray-100 flex items-center justify-center overflow-hidden">
                    @if($product->image)
                        <img src="{{ Storage::url($product->image) }}" alt="{{ $product->name_ar }}" class="w-full h-full object-cover">
                    @else
                        <span class="text-gray-400 text-4xl">🍽️</span>
                    @endif
                </div>

                <div class="p-4 flex flex-col flex-1">
                    <h3 class="font-bold text-gray-800">{{ $product->name_ar }}</h3>
                    <p class="text-sm text-gray-500">{{ $product->category->name_ar ?? '' }}</p>

                    <div class="flex items-center gap-2 mt-2">
                        <span class="text-red-700 font-bold">{{ number_format($product->discounted_price ?? $product->price, 3) }} JOD</span>
                        @if($product->discounted_price)
                            <span class="line-through text-gray-400 text-sm">{{ number_format($product->price, 3) }}</span>
                        @endif
                    </div>

                    <div class="mt-auto pt-4 flex gap-2">
                        <!-- Toggle Availability -->
                        <button wire:click="toggleAvailability({{ $product->id }})"
                            class="flex-1 py-1.5 text-sm font-bold rounded-lg transition {{ $product->is_available ? 'bg-green-100 text-green-700 hover:bg-green-200' : 'bg-gray-100 text-gray-500 hover:bg-gray-200' }}">
                            {{ $product->is_available ? '✓ متاح' : '✗ غير متاح' }}
                        </button>

                        <button wire:click="manageOptions({{ $product->id }})"
                            class="px-3 py-1.5 text-sm font-bold rounded-lg bg-orange-100 text-orange-700 hover:bg-orange-200 transition" title="إدارة الخيارات">
                            خيارات
                        </button>

                        <button wire:click="showEditForm({{ $product->id }})"
                            class="px-3 py-1.5 text-sm font-bold rounded-lg bg-blue-100 text-blue-700 hover:bg-blue-200 transition">
                            تعديل
                        </button>

                        <button wire:click="deleteProduct({{ $product->id }})"
                            wire:confirm="هل أنت متأكد من حذف هذا الصنف؟"
                            class="px-3 py-1.5 text-sm font-bold rounded-lg bg-red-100 text-red-700 hover:bg-red-200 transition">
                            حذف
                        </button>
                    </div>
                </div>
            </div>
        @empty
            <div class="col-span-full text-center py-16 text-gray-400">
                لا يوجد أصناف مطابقة.
            </div>
        @endforelse
    </div>

    <div>{{ $products->links() }}</div>

    <!-- Create / Edit Form Modal -->
    @if($formMode)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60" wire:click.self="closeForm">
            <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4">
                <div class="flex items-center justify-between p-6 border-b">
                    <h3 class="text-xl font-bold text-gray-800">
                        {{ $formMode === 'create' ? 'إضافة صنف جديد' : 'تعديل الصنف' }}
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

                    <div class="grid grid-cols-2 gap-5">
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">الوصف بالعربية</label>
                            <textarea wire:model="descriptionAr" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none"></textarea>
                        </div>
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">الوصف بالإنجليزية</label>
                            <textarea wire:model="descriptionEn" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none" dir="ltr"></textarea>
                        </div>
                    </div>

                    <div class="grid grid-cols-3 gap-5">
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">السعر (JOD) *</label>
                            <input wire:model="price" type="number" step="0.001" min="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
                            @error('price') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">سعر العرض (JOD)</label>
                            <input wire:model="discountedPrice" type="number" step="0.001" min="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">القسم *</label>
                            <select wire:model="categoryId" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
                                <option value="">اختر القسم</option>
                                @foreach($categories as $cat)
                                    <option value="{{ $cat->id }}">{{ $cat->name_ar }}</option>
                                @endforeach
                            </select>
                            @error('categoryId') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-5 items-center">
                        <div>
                            <label class="block text-sm font-semibold text-gray-700 mb-1">صورة الصنف</label>
                            <input wire:model="image" type="file" accept="image/*" class="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:bg-red-100 file:text-red-700 file:font-semibold hover:file:bg-red-200">
                            @error('image') <span class="text-red-500 text-xs">{{ $message }}</span> @enderror
                        </div>
                        <div class="flex items-center gap-3 mt-5">
                            <input wire:model="isAvailable" type="checkbox" id="isAvailable" class="w-5 h-5 text-red-600 rounded focus:ring-red-500">
                            <label for="isAvailable" class="text-sm font-semibold text-gray-700">متاح للطلب</label>
                        </div>
                    </div>

                    <div class="flex justify-end gap-3 pt-4 border-t">
                        <button type="button" wire:click="closeForm" class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-semibold">
                            إلغاء
                        </button>
                        <button type="submit" class="px-8 py-2 bg-red-700 text-white rounded-lg hover:bg-red-800 font-bold transition">
                            {{ $formMode === 'create' ? 'إضافة الصنف' : 'حفظ التعديلات' }}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    @endif

    <!-- Manage Options Modal -->
    @if($managingOptionsForProductId)
        <div class="fixed inset-0 z-[60] flex items-center justify-center bg-black bg-opacity-60" wire:click.self="closeOptionsManager">
            <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[95vh] overflow-y-auto m-4 flex flex-col">
                <div class="flex items-center justify-between p-6 border-b shrink-0">
                    <h3 class="text-xl font-bold text-gray-800">إدارة خيارات الصنف</h3>
                    <button wire:click="closeOptionsManager" class="text-gray-400 hover:text-gray-600 text-2xl leading-none">&times;</button>
                </div>

                <div class="p-6 flex-1 overflow-y-auto space-y-6">
                    <!-- Add Group Form -->
                    <div class="bg-gray-50 border border-gray-200 p-4 rounded-xl">
                        <h4 class="font-bold mb-3">إضافة مجموعة خيارات جديدة (مثال: الإضافات، الحجم)</h4>
                        <div class="grid grid-cols-5 gap-3 items-end">
                            <div class="col-span-2">
                                <label class="block text-xs font-semibold text-gray-600 mb-1">الاسم بالعربية</label>
                                <input wire:model="newGroupNameAr" type="text" class="w-full px-3 py-1.5 border rounded focus:ring-1 focus:ring-red-500">
                            </div>
                            <div class="col-span-2">
                                <label class="block text-xs font-semibold text-gray-600 mb-1">الاسم بالإنجليزية</label>
                                <input wire:model="newGroupNameEn" type="text" class="w-full px-3 py-1.5 border rounded focus:ring-1 focus:ring-red-500">
                            </div>
                            <div>
                                <label class="block text-xs font-semibold text-gray-600 mb-1">النوع</label>
                                <select wire:model="newGroupType" class="w-full px-3 py-1.5 border rounded focus:ring-1 focus:ring-red-500">
                                    <option value="checkbox">متعدد (Checkbox)</option>
                                    <option value="radio">مفرد (Radio)</option>
                                </select>
                            </div>
                        </div>
                        <div class="flex items-center gap-4 mt-3">
                            <label class="flex items-center gap-2 text-sm"><input type="checkbox" wire:model="newGroupRequired"> مطلوب للإضافة للسلة</label>
                            <button wire:click="addOptionGroup" class="ml-auto px-4 py-1.5 bg-gray-800 text-white rounded font-semibold text-sm hover:bg-gray-900">إضافة المجموعة</button>
                        </div>
                    </div>

                    <!-- Groups List -->
                    @forelse($optionGroups as $group)
                        <div class="border border-gray-200 rounded-xl overflow-hidden">
                            <div class="bg-gray-100 px-4 py-3 flex justify-between items-center border-b border-gray-200">
                                <div>
                                    <span class="font-bold text-lg">{{ $group['name_ar'] }}</span>
                                    <span class="text-xs ml-2 px-2 py-0.5 bg-gray-200 rounded-full text-gray-700">
                                        {{ $group['type'] === 'checkbox' ? 'متعدد الاختيار' : 'اختيار وحيد' }}
                                    </span>
                                    @if($group['is_required']) <span class="text-xs text-red-600 font-bold ml-1">مطلوب</span> @endif
                                </div>
                                <button wire:click="deleteOptionGroup({{ $group['id'] }})" wire:confirm="حذف المجموعة سيحذف كل الخيارات بداخلها. متأكد؟" class="text-red-500 hover:text-red-700 text-sm font-semibold">حذف المجموعة</button>
                            </div>
                            
                            <div class="p-4">
                                <table class="w-full text-right text-sm">
                                    <thead class="text-gray-500">
                                        <tr>
                                            <th class="pb-2 w-1/4">الاسم بالعربية</th>
                                            <th class="pb-2 w-1/4">الاسم بالإنجليزية</th>
                                            <th class="pb-2 w-1/6">السعر الإضافي</th>
                                            <th class="pb-2 w-1/6">تحديد تلقائي</th>
                                            <th class="pb-2 w-1/6">إجراء</th>
                                        </tr>
                                    </thead>
                                    <tbody class="divide-y divide-gray-100">
                                        @foreach($group['all_options'] as $opt)
                                            <tr>
                                                <td class="py-2">{{ $opt['name_ar'] }}</td>
                                                <td class="py-2" dir="ltr">{{ $opt['name_en'] }}</td>
                                                <td class="py-2 font-bold">{{ number_format($opt['price'], 3) }}</td>
                                                <td class="py-2">
                                                    @if($opt['default_selected']) <span class="text-green-600 font-bold">نعم</span> @else - @endif
                                                </td>
                                                <td class="py-2">
                                                    <button wire:click="deleteOption({{ $opt['id'] }})" class="text-red-500 hover:underline">حذف</button>
                                                </td>
                                            </tr>
                                        @endforeach
                                    </tbody>
                                </table>

                                <!-- Add Option Inline Form -->
                                <div class="mt-4 pt-4 border-t border-gray-100 flex gap-2 items-center" x-data="{ optAr: '', optEn: '', optPrice: '0', optDefault: false }">
                                    <input x-model="optAr" type="text" placeholder="الاسم بالعربية" class="flex-1 px-3 py-1.5 border rounded text-sm focus:outline-none">
                                    <input x-model="optEn" type="text" placeholder="الاسم بالإنجليزية" class="flex-1 px-3 py-1.5 border rounded text-sm focus:outline-none">
                                    <input x-model="optPrice" type="number" step="0.001" placeholder="السعر" class="w-24 px-3 py-1.5 border rounded text-sm focus:outline-none">
                                    <label class="flex items-center gap-1 text-xs whitespace-nowrap px-2"><input x-model="optDefault" type="checkbox"> محدد مسبقاً</label>
                                    <button @click="$wire.addOption({{ $group['id'] }}, optAr, optEn, parseFloat(optPrice||0), optDefault, true).then(() => {optAr=''; optEn=''; optPrice='0'; optDefault=false})" 
                                        class="px-4 py-1.5 bg-red-50 text-red-700 border border-red-200 rounded font-semibold text-sm hover:bg-red-100 whitespace-nowrap">
                                        + إضافة خيار
                                    </button>
                                </div>
                            </div>
                        </div>
                    @empty
                        <p class="text-center text-gray-500 py-8 border-2 border-dashed rounded-xl">لا يوجد مجموعات خيارات لهذا الصنف بعد.</p>
                    @endforelse
                </div>
            </div>
        </div>
    @endif
</div>
