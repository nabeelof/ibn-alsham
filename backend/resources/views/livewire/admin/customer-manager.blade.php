@php $money = fn ($v) => number_format((float) $v, 3) . ' د.أ'; @endphp

<div>
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <x-admin.stat label="إجمالي الزبائن" :value="$totalCustomers" tone="blue" icon="users" />
        <x-admin.stat label="حسابات مُفعّلة" :value="$verifiedCustomers" tone="green" />
        <x-admin.stat label="مشتركون في الإشعارات" :value="$withPushToken" tone="amber" icon="bell" />
    </div>

    <div class="bg-white rounded-xl border border-slate-200 p-4 mb-6 flex flex-wrap items-end gap-3">
        <div class="flex-1 min-w-[200px]">
            <label class="block text-[11px] font-bold text-slate-500 mb-1">بحث</label>
            <input type="text" wire:model.live.debounce.400ms="search" placeholder="الاسم أو رقم الهاتف…"
                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
        </div>
        <div>
            <label class="block text-[11px] font-bold text-slate-500 mb-1">الترتيب</label>
            <select wire:model.live="sort" class="px-3 py-2 text-sm border border-slate-300 rounded-lg">
                <option value="recent">الأحدث تسجيلاً</option>
                <option value="spend">الأعلى إنفاقاً</option>
                <option value="orders">الأكثر طلباً</option>
            </select>
        </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table class="w-full text-right">
            <thead class="bg-slate-50 text-slate-500 text-xs">
                <tr>
                    <th class="px-5 py-3 font-bold">الزبون</th>
                    <th class="px-5 py-3 font-bold">الهاتف</th>
                    <th class="px-5 py-3 font-bold">عدد الطلبات</th>
                    <th class="px-5 py-3 font-bold">إجمالي الإنفاق</th>
                    <th class="px-5 py-3 font-bold">مسجّل منذ</th>
                    <th class="px-5 py-3 font-bold"></th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($customers as $customer)
                    <tr class="hover:bg-slate-50">
                        <td class="px-5 py-3">
                            <p class="text-sm font-bold text-slate-800">{{ $customer->name }}</p>
                            @if($customer->fcm_token)
                                <span class="text-[10px] font-bold text-emerald-600">إشعارات مفعّلة</span>
                            @endif
                        </td>
                        <td class="px-5 py-3 text-sm text-slate-600" dir="ltr">{{ $customer->phone }}</td>
                        <td class="px-5 py-3 text-sm text-slate-600">{{ $customer->orders_count }}</td>
                        <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($customer->spent ?? 0) }}</td>
                        <td class="px-5 py-3 text-xs text-slate-500">{{ $customer->created_at->diffForHumans() }}</td>
                        <td class="px-5 py-3">
                            <button wire:click="view({{ $customer->id }})"
                                    class="text-xs font-bold text-blue-600 hover:underline">التفاصيل</button>
                        </td>
                    </tr>
                @empty
                    <tr><td colspan="6" class="px-5 py-14 text-center text-sm text-slate-400">لا يوجد زبائن</td></tr>
                @endforelse
            </tbody>
        </table>
        <div class="px-5 py-3 border-t border-slate-100">{{ $customers->links() }}</div>
    </div>

    @if($viewing)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-2xl max-h-[92vh] overflow-y-auto">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <div>
                        <h3 class="font-bold text-slate-800">{{ $viewing->name }}</h3>
                        <p class="text-xs text-slate-400" dir="ltr">{{ $viewing->phone }}</p>
                    </div>
                    <button wire:click="closeModal" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <div class="p-6">
                    <h4 class="text-sm font-bold text-slate-700 mb-3">آخر الطلبات</h4>
                    <table class="w-full text-right">
                        <tbody class="divide-y divide-slate-100">
                            @forelse($viewingOrders as $order)
                                <tr>
                                    <td class="py-2.5 text-sm font-semibold text-slate-800">{{ $order->order_number }}</td>
                                    <td class="py-2.5 text-xs text-slate-500">{{ $order->created_at->format('Y-m-d') }}</td>
                                    <td class="py-2.5 text-xs">
                                        <span class="px-2 py-0.5 font-bold rounded-full bg-slate-100 text-slate-600">
                                            {{ $order->status_label_ar }}
                                        </span>
                                    </td>
                                    <td class="py-2.5 text-sm font-bold text-slate-900">{{ $money($order->total) }}</td>
                                </tr>
                            @empty
                                <tr><td class="py-8 text-center text-sm text-slate-400">لا توجد طلبات</td></tr>
                            @endforelse
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    @endif
</div>
