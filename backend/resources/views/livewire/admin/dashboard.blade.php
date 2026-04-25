<div>
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white p-6 rounded-xl shadow border border-gray-100 flex flex-col items-center">
            <span class="text-gray-500 text-sm font-semibold">المستخدمين</span>
            <span class="text-3xl font-bold text-gray-800 mt-2">{{ $totalUsers }}</span>
        </div>
        
        <div class="bg-white p-6 rounded-xl shadow border border-gray-100 flex flex-col items-center">
            <span class="text-gray-500 text-sm font-semibold">إجمالي الطلبات</span>
            <span class="text-3xl font-bold text-gray-800 mt-2">{{ $totalOrders }}</span>
        </div>

        <div class="bg-white p-6 rounded-xl shadow border border-gray-100 flex flex-col items-center">
            <span class="text-gray-500 text-sm font-semibold">طلبات اليوم</span>
            <span class="text-3xl font-bold text-red-600 mt-2">{{ $todayOrders }}</span>
        </div>

        <div class="bg-white p-6 rounded-xl shadow border border-gray-100 flex flex-col items-center">
            <span class="text-gray-500 text-sm font-semibold">إيرادات اليوم</span>
            <span class="text-3xl font-bold text-green-600 mt-2">{{ rtrim(rtrim(number_format($todayRevenue, 3), '0'), '.') }} JOD</span>
        </div>
    </div>

    <!-- Recent Orders -->
    <div class="bg-white rounded-xl shadow border border-gray-100 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50">
            <h3 class="font-bold text-gray-800">أحدث الطلبات</h3>
            <a href="{{ route('admin.orders') }}" class="text-sm font-semibold text-red-700 hover:underline">عرض الكل</a>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full text-right divide-y divide-gray-200">
                <thead class="bg-gray-50 text-gray-500 text-sm">
                    <tr>
                        <th class="px-6 py-3 font-semibold">رقم الطلب</th>
                        <th class="px-6 py-3 font-semibold">الزبون</th>
                        <th class="px-6 py-3 font-semibold">النوع</th>
                        <th class="px-6 py-3 font-semibold">الإجمالي</th>
                        <th class="px-6 py-3 font-semibold">الحالة</th>
                        <th class="px-6 py-3 font-semibold">الوقت</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 bg-white">
                    @forelse($recentOrders as $order)
                        <tr class="hover:bg-gray-50">
                            <td class="px-6 py-4 font-semibold text-gray-800">{{ $order->order_number }}</td>
                            <td class="px-6 py-4">{{ $order->user->name ?: $order->user->phone }}</td>
                            <td class="px-6 py-4 text-sm">{{ $order->type === 'delivery' ? 'توصيل' : 'استلام' }}</td>
                            <td class="px-6 py-4 font-bold text-gray-700">د.أ {{ rtrim(rtrim(number_format($order->total, 3), '0'), '.') }}</td>
                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="px-3 py-1 text-xs font-bold rounded-full 
                                    {{ $order->status === 'pending' ? 'bg-yellow-100 text-yellow-800' : '' }}
                                    {{ in_array($order->status, ['confirmed', 'preparing', 'ready', 'out_for_delivery']) ? 'bg-blue-100 text-blue-800' : '' }}
                                    {{ in_array($order->status, ['delivered', 'completed']) ? 'bg-green-100 text-green-800' : '' }}
                                    {{ $order->status === 'cancelled' ? 'bg-red-100 text-red-800' : '' }}">
                                    {{ $order->status_label_ar }}
                                </span>
                            </td>
                            <td class="px-6 py-4 text-sm text-gray-500">{{ $order->created_at->diffForHumans() }}</td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="px-6 py-8 text-center text-gray-500">لا يوجد طلبات بعد.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>
