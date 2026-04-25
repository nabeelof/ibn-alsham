<div>
    @if(session('message'))
        <div class="mb-4 p-4 bg-green-100 border border-green-300 text-green-800 rounded-lg font-semibold">
            {{ session('message') }}
        </div>
    @endif

    <!-- Filters -->
    <div class="flex flex-col md:flex-row gap-4 mb-6">
        <input
            wire:model.live.debounce.300ms="search"
            type="text"
            placeholder="بحث برقم الطلب..."
            class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none"
        >
        <select wire:model.live="statusFilter" class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none">
            <option value="">كل الحالات</option>
            <option value="pending">بانتظار التأكيد</option>
            <option value="confirmed">مؤكد</option>
            <option value="preparing">قيد التحضير</option>
            <option value="ready">جاهز</option>
            <option value="out_for_delivery">في الطريق</option>
            <option value="delivered">تم التوصيل</option>
            <option value="completed">مكتمل</option>
            <option value="cancelled">ملغي</option>
        </select>
    </div>

    <!-- Orders Table -->
    <div class="bg-white rounded-xl shadow border border-gray-100 overflow-hidden">
        <div class="overflow-x-auto">
            <table class="w-full text-right divide-y divide-gray-200">
                <thead class="bg-gray-50 text-gray-500 text-sm">
                    <tr>
                        <th class="px-6 py-3 font-semibold">رقم الطلب</th>
                        <th class="px-6 py-3 font-semibold">الزبون</th>
                        <th class="px-6 py-3 font-semibold">النوع</th>
                        <th class="px-6 py-3 font-semibold">الإجمالي</th>
                        <th class="px-6 py-3 font-semibold">الحالة</th>
                        <th class="px-6 py-3 font-semibold">التاريخ</th>
                        <th class="px-6 py-3 font-semibold">إجراء</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 bg-white">
                    @forelse($orders as $order)
                        <tr class="hover:bg-gray-50">
                            <td class="px-6 py-4 font-semibold text-gray-800">
                                <button wire:click="viewOrder({{ $order->id }})" class="text-red-700 hover:underline">
                                    {{ $order->order_number }}
                                </button>
                            </td>
                            <td class="px-6 py-4">{{ $order->user->name ?: $order->user->phone }}</td>
                            <td class="px-6 py-4 text-sm">{{ $order->type === 'delivery' ? '🚗 توصيل' : '🏪 استلام' }}</td>
                            <td class="px-6 py-4 font-bold">{{ number_format($order->total, 3) }} JOD</td>
                            <td class="px-6 py-4 flex items-center gap-2">
                                @if($order->status === 'pending')
                                    <button wire:click="showConfirmModal({{ $order->id }})" class="text-sm bg-blue-600 text-white px-3 py-1.5 rounded-lg font-bold hover:bg-blue-700">
                                        تأكيد الطلب
                                    </button>
                                @endif
                                <select wire:change="updateStatus({{ $order->id }}, $event.target.value)"
                                    class="px-2 py-1.5 text-sm border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-500">
                                    @foreach(['pending'=>'بانتظار التأكيد','confirmed'=>'مؤكد','preparing'=>'قيد التحضير','ready'=>'جاهز','out_for_delivery'=>'في الطريق','delivered'=>'تم التوصيل','completed'=>'مكتمل','cancelled'=>'ملغي'] as $val => $label)
                                        <option value="{{ $val }}" {{ $order->status === $val ? 'selected' : '' }}>{{ $label }}</option>
                                    @endforeach
                                </select>
                                <button wire:click="viewOrder({{ $order->id }})" class="text-sm text-gray-600 border px-2 py-1.5 rounded-lg hover:bg-gray-50">
                                    تفاصيل
                                </button>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" class="px-6 py-12 text-center text-gray-400">لا يوجد طلبات مطابقة.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        <div class="px-6 py-4 border-t border-gray-100">
            {{ $orders->links() }}
        </div>
    </div>

    <!-- Order Detail Modal -->
    @if($viewingOrder)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50" wire:click.self="closeModal">
            <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4">
                <div class="flex items-center justify-between p-6 border-b">
                    <h3 class="text-xl font-bold text-gray-800">تفاصيل الطلب: {{ $viewingOrder->order_number }}</h3>
                    <button wire:click="closeModal" class="text-gray-400 hover:text-gray-600 text-2xl leading-none">&times;</button>
                </div>

                <div class="p-6 space-y-6">
                    <!-- Customer Info -->
                    <div class="grid grid-cols-2 gap-4 bg-gray-50 rounded-xl p-4">
                        <div>
                            <p class="text-xs text-gray-500 font-semibold mb-1">اسم الزبون</p>
                            <p class="font-bold">{{ $viewingOrder->user->name ?: 'غير محدد' }}</p>
                        </div>
                        <div>
                            <p class="text-xs text-gray-500 font-semibold mb-1">رقم الهاتف</p>
                            <p class="font-bold">{{ $viewingOrder->user->phone }}</p>
                        </div>
                        <div>
                            <p class="text-xs text-gray-500 font-semibold mb-1">نوع الطلب</p>
                            <p class="font-bold">{{ $viewingOrder->type === 'delivery' ? 'توصيل' : 'استلام من الفرع' }}</p>
                        </div>
                        @if($viewingOrder->address)
                            <div class="col-span-2">
                                <p class="text-xs text-gray-500 font-semibold mb-1">العنوان</p>
                                <p class="font-bold">{{ $viewingOrder->address }}</p>
                            </div>
                        @endif
                        @if($viewingOrder->notes)
                            <div class="col-span-2 bg-yellow-50 p-3 rounded-lg border border-yellow-200">
                                <p class="text-xs text-yellow-800 font-semibold mb-1">ملاحظات الزبون</p>
                                <p class="font-bold text-red-600">{{ $viewingOrder->notes }}</p>
                            </div>
                        @endif
                        @if($viewingOrder->rating)
                            <div class="col-span-2 mt-2 bg-blue-50 p-4 rounded-xl border border-blue-200">
                                <p class="text-sm text-blue-800 font-semibold mb-1">تقييم الزبون ({{ $viewingOrder->rating }} من 5 ⭐)</p>
                                @if($viewingOrder->review_note)
                                    <p class="font-medium text-gray-700 italic">"{{ $viewingOrder->review_note }}"</p>
                                @endif
                            </div>
                        @endif
                    </div>

                    <!-- Items -->
                    <div>
                        <h4 class="font-bold text-gray-700 mb-3">العناصر المطلوبة</h4>
                        <div class="space-y-3">
                            @foreach($viewingOrder->items as $item)
                                <div class="py-3 border-b border-gray-100">
                                    <div class="flex justify-between items-start">
                                        <div>
                                            <p class="font-bold">{{ $item->product_name_ar }}</p>
                                            <p class="text-sm text-gray-500">{{ number_format($item->unit_price, 3) }} JOD × {{ $item->quantity }}</p>
                                            
                                            @if($item->options->isNotEmpty())
                                                <div class="mt-2 pl-4 border-r-2 border-gray-200">
                                                    @foreach($item->options as $opt)
                                                        <p class="text-xs text-gray-600">
                                                            + {{ $opt->option_name_ar }} <span class="text-gray-400">({{ number_format($opt->price, 3) }} JOD)</span>
                                                        </p>
                                                    @endforeach
                                                </div>
                                            @endif
                                        </div>
                                        <p class="font-bold text-red-700">{{ number_format($item->total, 3) }} JOD</p>
                                    </div>
                                </div>
                            @endforeach
                        </div>
                    </div>

                    <!-- Totals -->
                    <div class="bg-gray-50 rounded-xl p-4 space-y-2">
                        <div class="flex justify-between"><span class="text-gray-600">المجموع الفرعي</span><span class="font-semibold">{{ number_format($viewingOrder->subtotal, 3) }} JOD</span></div>
                        @if($viewingOrder->delivery_fee > 0)
                            <div class="flex justify-between"><span class="text-gray-600">رسوم التوصيل</span><span class="font-semibold">{{ number_format($viewingOrder->delivery_fee, 3) }} JOD</span></div>
                        @endif
                        <div class="flex justify-between border-t pt-2"><span class="font-bold text-lg">الإجمالي</span><span class="font-bold text-lg text-red-700">{{ number_format($viewingOrder->total, 3) }} JOD</span></div>
                    </div>
                </div>
            </div>
        </div>
    @endif

    <!-- Confirm Modal -->
    @if($confirmingOrder)
        <div class="fixed inset-0 z-[60] flex items-center justify-center bg-black bg-opacity-60" wire:click.self="cancelConfirmModal">
            <div class="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 m-4">
                <h3 class="text-xl font-bold text-gray-800 mb-2">تأكيد الطلب</h3>
                <p class="text-sm text-gray-500 mb-6">الطلب رقم: #{{ $confirmingOrder->order_number }}</p>
                
                <div class="mb-6">
                    <label class="block text-sm font-semibold text-gray-700 mb-2">الوقت التقديري للتجهيز (دقيقة)</label>
                    <input wire:model="estimatedTime" type="number" min="5" max="180" step="5" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none">
                </div>

                <div class="flex gap-3">
                    <button wire:click="cancelConfirmModal" class="flex-1 py-2 bg-gray-100 text-gray-700 font-bold rounded-lg hover:bg-gray-200 transition">
                        إلغاء
                    </button>
                    <button wire:click="confirmOrder" class="flex-1 py-2 bg-blue-600 text-white font-bold rounded-lg hover:bg-blue-700 transition">
                        تأكيد
                    </button>
                </div>
            </div>
        </div>
    @endif
</div>
