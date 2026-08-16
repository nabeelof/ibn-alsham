<div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
    <!-- Composer -->
    <div class="xl:col-span-1 space-y-4">
        <div class="grid grid-cols-2 gap-4">
            <x-admin.stat label="إجمالي الزبائن" :value="$totalCustomers" tone="blue" />
            <x-admin.stat label="يستقبلون الإشعارات" :value="$reachable" tone="green" icon="bell" />
        </div>

        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-3.5 border-b border-slate-100 bg-slate-50">
                <h3 class="font-bold text-slate-800">إرسال إشعار</h3>
            </div>
            <form wire:submit="send" class="p-5 space-y-4">
                <div>
                    <label class="block text-xs font-bold text-slate-600 mb-1">المستلمون</label>
                    <select wire:model.live="audience" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        <option value="all">جميع الزبائن</option>
                        <option value="single">زبون محدّد</option>
                    </select>
                </div>

                @if($audience === 'single')
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الزبون</label>
                        <select wire:model="userId" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                            <option value="">— اختر —</option>
                            @foreach($customers as $customer)
                                <option value="{{ $customer->id }}">{{ $customer->name }} — {{ $customer->phone }}</option>
                            @endforeach
                        </select>
                        @error('userId') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>
                @endif

                <div>
                    <label class="block text-xs font-bold text-slate-600 mb-1">النوع</label>
                    <select wire:model="type" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        <option value="promotion">عرض ترويجي</option>
                        <option value="general">إشعار عام</option>
                    </select>
                </div>

                <div>
                    <label class="block text-xs font-bold text-slate-600 mb-1">العنوان بالعربية</label>
                    <input type="text" wire:model="titleAr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    @error('titleAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                </div>
                <div>
                    <label class="block text-xs font-bold text-slate-600 mb-1">النص بالعربية</label>
                    <textarea wire:model="bodyAr" rows="3" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg"></textarea>
                    @error('bodyAr') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                </div>

                <div>
                    <label class="block text-xs font-bold text-slate-600 mb-1">العنوان بالإنجليزية</label>
                    <input type="text" wire:model="titleEn" dir="ltr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                    @error('titleEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                </div>
                <div>
                    <label class="block text-xs font-bold text-slate-600 mb-1">النص بالإنجليزية</label>
                    <textarea wire:model="bodyEn" rows="3" dir="ltr" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg"></textarea>
                    @error('bodyEn') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                </div>

                <button type="submit" class="w-full px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800">
                    إرسال الإشعار
                </button>

                @if($reachable === 0)
                    <p class="text-[11px] text-amber-700 bg-amber-50 border border-amber-100 rounded-lg p-2.5">
                        لا يوجد زبائن مشتركون في الإشعارات بعد. سيُحفظ الإشعار داخل التطبيق لكن لن يصل كإشعار منبثق حتى يتم ربط Firebase.
                    </p>
                @endif
            </form>
        </div>
    </div>

    <!-- History -->
    <div class="xl:col-span-2">
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-3.5 border-b border-slate-100 bg-slate-50">
                <h3 class="font-bold text-slate-800">سجلّ الإشعارات</h3>
            </div>
            <table class="w-full text-right">
                <thead class="bg-slate-50 text-slate-500 text-xs">
                    <tr>
                        <th class="px-5 py-3 font-bold">العنوان</th>
                        <th class="px-5 py-3 font-bold">المستلم</th>
                        <th class="px-5 py-3 font-bold">النوع</th>
                        <th class="px-5 py-3 font-bold">الحالة</th>
                        <th class="px-5 py-3 font-bold">التاريخ</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    @forelse($recent as $notification)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3">
                                <p class="text-sm font-semibold text-slate-800">{{ $notification->title_ar }}</p>
                                <p class="text-[11px] text-slate-400 line-clamp-1">{{ $notification->body_ar }}</p>
                            </td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $notification->user?->name ?? 'الجميع' }}</td>
                            <td class="px-5 py-3">
                                <span class="px-2 py-0.5 text-[10px] font-bold rounded-full bg-slate-100 text-slate-600">
                                    {{ ['order' => 'طلب', 'promotion' => 'عرض', 'general' => 'عام'][$notification->type] ?? $notification->type }}
                                </span>
                            </td>
                            <td class="px-5 py-3 text-xs {{ $notification->read_at ? 'text-slate-400' : 'text-emerald-600 font-bold' }}">
                                {{ $notification->read_at ? 'مقروء' : 'غير مقروء' }}
                            </td>
                            <td class="px-5 py-3 text-xs text-slate-500">{{ $notification->created_at->diffForHumans() }}</td>
                        </tr>
                    @empty
                        <tr><td colspan="5" class="px-5 py-14 text-center text-sm text-slate-400">لم يُرسل أي إشعار بعد</td></tr>
                    @endforelse
                </tbody>
            </table>
            <div class="px-5 py-3 border-t border-slate-100">{{ $recent->links() }}</div>
        </div>
    </div>
</div>
