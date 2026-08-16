@php
    $money = fn ($v) => number_format((float) $v, 3) . ' د.أ';
@endphp

<div>
    <x-admin.range-filter :ranges="$ranges" :range="$range" />

    <!-- Today at a glance -->
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <x-admin.stat label="طلبات اليوم" :value="$today['orders_count']" tone="red" icon="receipt"
                      :sub="$pendingOrders . ' بانتظار التأكيد'" />
        <x-admin.stat label="مبيعات اليوم" :value="$money($today['revenue'])" tone="green" icon="wallet"
                      :sub="'متوسط الطلب ' . $money($today['average_order'])" />
        <x-admin.stat label="طلبات قيد التنفيذ" :value="$activeOrders" tone="amber" icon="truck"
                      sub="مؤكدة أو قيد التحضير" />
        <x-admin.stat label="عدد الزبائن" :value="$totalCustomers" tone="blue" icon="users"
                      :sub="$outOfStock . ' منتج غير متاح'" />
    </div>

    <!-- Period summary -->
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <x-admin.stat label="إجمالي المبيعات" :value="$money($summary['revenue'])" tone="slate"
                      :sub="$summary['orders_count'] . ' طلب'" />
        <x-admin.stat label="الخصومات" :value="$money($summary['discounts'])" tone="amber"
                      sub="كوبونات وعروض" />
        <x-admin.stat label="رسوم التوصيل" :value="$money($summary['delivery'])" tone="blue" />
        @if($canViewAccounting)
            <x-admin.stat label="صافي الربح" :value="$money($summary['net_profit'])"
                          :tone="$summary['net_profit'] >= 0 ? 'green' : 'red'"
                          :sub="'هامش ' . number_format($summary['margin'], 1) . '%'" />
        @else
            <x-admin.stat label="متوسط قيمة الطلب" :value="$money($summary['average_order'])" tone="slate" />
        @endif
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-6">
        <!-- Sales chart -->
        <div class="xl:col-span-2 bg-white rounded-xl border border-slate-200 p-5">
            <h3 class="font-bold text-slate-800 mb-4">حركة المبيعات</h3>
            <canvas id="salesChart" height="110"
                    data-labels='@json(array_column($daily, "label"))'
                    data-totals='@json(array_column($daily, "total"))'
                    data-orders='@json(array_column($daily, "orders"))'></canvas>
        </div>

        <!-- Payment breakdown -->
        <div class="bg-white rounded-xl border border-slate-200 p-5">
            <h3 class="font-bold text-slate-800 mb-4">التحصيل حسب وسيلة الدفع</h3>
            @forelse($paymentBreakdown as $row)
                <div class="flex items-center justify-between py-2.5 border-b border-slate-100 last:border-0">
                    <div>
                        <p class="text-sm font-semibold text-slate-700">{{ $row['label'] }}</p>
                        <p class="text-[11px] text-slate-400">{{ $row['orders'] }} طلب</p>
                    </div>
                    <span class="text-sm font-bold text-slate-900">{{ $money($row['total']) }}</span>
                </div>
            @empty
                <p class="text-sm text-slate-400 py-6 text-center">لا توجد مبيعات في هذه الفترة</p>
            @endforelse

            <h3 class="font-bold text-slate-800 mt-6 mb-3">نوع الطلب</h3>
            @foreach($typeSplit as $row)
                <div class="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
                    <span class="text-sm font-semibold text-slate-700">{{ $row['label'] }}</span>
                    <span class="text-sm font-bold text-slate-900">{{ $row['orders'] }} · {{ $money($row['total']) }}</span>
                </div>
            @endforeach
        </div>
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <!-- Live order queue -->
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-3.5 border-b border-slate-100 flex items-center justify-between bg-slate-50">
                <h3 class="font-bold text-slate-800">الطلبات الجارية</h3>
                <a href="{{ route('admin.orders') }}" class="text-xs font-bold text-red-700 hover:underline">عرض الكل</a>
            </div>
            <table class="w-full text-right">
                <tbody class="divide-y divide-slate-100">
                    @forelse($liveQueue as $order)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3">
                                <p class="text-sm font-bold text-slate-800">{{ $order->order_number }}</p>
                                <p class="text-[11px] text-slate-400">{{ $order->user?->name ?: $order->user?->phone }}</p>
                            </td>
                            <td class="px-3 py-3 text-sm font-semibold text-slate-700">{{ $money($order->total) }}</td>
                            <td class="px-5 py-3">
                                <span class="px-2.5 py-1 text-[11px] font-bold rounded-full
                                    {{ $order->status === 'pending' ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800' }}">
                                    {{ $order->status_label_ar }}
                                </span>
                            </td>
                        </tr>
                    @empty
                        <tr><td colspan="3" class="px-5 py-10 text-center text-sm text-slate-400">لا توجد طلبات جارية</td></tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        <!-- Best sellers -->
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-3.5 border-b border-slate-100 bg-slate-50">
                <h3 class="font-bold text-slate-800">الأكثر مبيعاً</h3>
            </div>
            <table class="w-full text-right">
                <tbody class="divide-y divide-slate-100">
                    @forelse($topProducts as $product)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3 text-sm font-semibold text-slate-700">{{ $product['name'] }}</td>
                            <td class="px-3 py-3 text-sm text-slate-500">{{ $product['qty'] }} قطعة</td>
                            <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($product['revenue']) }}</td>
                        </tr>
                    @empty
                        <tr><td colspan="3" class="px-5 py-10 text-center text-sm text-slate-400">لا توجد مبيعات بعد</td></tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>

    @script
    <script>
        // Re-render on every Livewire update so the chart follows the range filter.
        const render = () => {
            const el = document.getElementById('salesChart');
            if (!el) return;
            if (window._salesChart) window._salesChart.destroy();

            window._salesChart = new Chart(el, {
                type: 'line',
                data: {
                    labels: JSON.parse(el.dataset.labels),
                    datasets: [
                        {
                            label: 'المبيعات (د.أ)',
                            data: JSON.parse(el.dataset.totals),
                            borderColor: '#C62828',
                            backgroundColor: 'rgba(198,40,40,.10)',
                            fill: true,
                            tension: .35,
                            yAxisID: 'y',
                        },
                        {
                            label: 'عدد الطلبات',
                            data: JSON.parse(el.dataset.orders),
                            borderColor: '#2563EB',
                            borderDash: [5, 4],
                            fill: false,
                            tension: .35,
                            yAxisID: 'y1',
                        },
                    ],
                },
                options: {
                    responsive: true,
                    interaction: { mode: 'index', intersect: false },
                    plugins: { legend: { labels: { font: { family: 'Cairo' } } } },
                    scales: {
                        y: { position: 'right', beginAtZero: true, ticks: { font: { family: 'Cairo' } } },
                        y1: { position: 'left', beginAtZero: true, grid: { drawOnChartArea: false }, ticks: { precision: 0, font: { family: 'Cairo' } } },
                        x: { ticks: { font: { family: 'Cairo' } } },
                    },
                },
            });
        };

        render();
        Livewire.hook('morphed', render);
    </script>
    @endscript
</div>
