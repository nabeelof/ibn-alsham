@php
    $money = fn ($v) => number_format((float) $v, 3) . ' د.أ';
    $tabs = [
        'sales' => 'المبيعات',
        'products' => 'المنتجات',
        'categories' => 'الأقسام',
        'customers' => 'الزبائن',
        'coupons' => 'الكوبونات',
    ];
@endphp

<div>
    <x-admin.range-filter :ranges="$ranges" :range="$range" :show-custom="true">
        @if($canExport)
            <button wire:click="export"
                    class="px-4 py-2 text-sm font-bold text-white bg-emerald-600 rounded-lg hover:bg-emerald-700 transition">
                تصدير CSV
            </button>
        @endif
        <span class="text-xs font-semibold text-slate-400 self-center">{{ $periodLabel }}</span>
    </x-admin.range-filter>

    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <x-admin.stat label="عدد الطلبات" :value="$summary['orders_count']" tone="blue" />
        <x-admin.stat label="إجمالي المبيعات" :value="$money($summary['revenue'])" tone="green" />
        <x-admin.stat label="متوسط الطلب" :value="$money($summary['average_order'])" tone="slate" />
        <x-admin.stat label="إجمالي الخصومات" :value="$money($summary['discounts'])" tone="amber" />
    </div>

    <!-- Tabs -->
    <div class="flex flex-wrap gap-2 mb-5">
        @foreach($tabs as $key => $label)
            <button wire:click="selectTab('{{ $key }}')"
                    class="px-4 py-2 text-sm font-bold rounded-lg transition
                           {{ $tab === $key ? 'bg-red-700 text-white' : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50' }}">
                {{ $label }}
            </button>
        @endforeach
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        @if($tab === 'sales')
            <div class="p-5 border-b border-slate-100">
                <canvas id="reportChart" height="90"
                        data-labels='@json(array_column($daily, "label"))'
                        data-totals='@json(array_column($daily, "total"))'></canvas>
            </div>
            <table class="w-full text-right">
                <thead class="bg-slate-50 text-slate-500 text-xs">
                    <tr>
                        <th class="px-5 py-3 font-bold">التاريخ</th>
                        <th class="px-5 py-3 font-bold">عدد الطلبات</th>
                        <th class="px-5 py-3 font-bold">المبيعات</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    @foreach(array_reverse($daily) as $row)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3 text-sm text-slate-700">{{ $row['label'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $row['orders'] }}</td>
                            <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($row['total']) }}</td>
                        </tr>
                    @endforeach
                </tbody>
            </table>

        @elseif($tab === 'products')
            <table class="w-full text-right">
                <thead class="bg-slate-50 text-slate-500 text-xs">
                    <tr>
                        <th class="px-5 py-3 font-bold">المنتج</th>
                        <th class="px-5 py-3 font-bold">الكمية</th>
                        <th class="px-5 py-3 font-bold">المبيعات</th>
                        <th class="px-5 py-3 font-bold">الربح التقديري</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    @forelse($topProducts as $row)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3 text-sm font-semibold text-slate-700">{{ $row['name'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $row['qty'] }}</td>
                            <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($row['revenue']) }}</td>
                            <td class="px-5 py-3 text-sm font-bold text-emerald-600">{{ $money($row['profit']) }}</td>
                        </tr>
                    @empty
                        <tr><td colspan="4" class="px-5 py-12 text-center text-sm text-slate-400">لا توجد بيانات</td></tr>
                    @endforelse
                </tbody>
            </table>

        @elseif($tab === 'categories')
            <table class="w-full text-right">
                <thead class="bg-slate-50 text-slate-500 text-xs">
                    <tr>
                        <th class="px-5 py-3 font-bold">القسم</th>
                        <th class="px-5 py-3 font-bold">الكمية المباعة</th>
                        <th class="px-5 py-3 font-bold">المبيعات</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    @forelse($categories as $row)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3 text-sm font-semibold text-slate-700">{{ $row['name'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $row['qty'] }}</td>
                            <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($row['revenue']) }}</td>
                        </tr>
                    @empty
                        <tr><td colspan="3" class="px-5 py-12 text-center text-sm text-slate-400">لا توجد بيانات</td></tr>
                    @endforelse
                </tbody>
            </table>

        @elseif($tab === 'customers')
            <table class="w-full text-right">
                <thead class="bg-slate-50 text-slate-500 text-xs">
                    <tr>
                        <th class="px-5 py-3 font-bold">الزبون</th>
                        <th class="px-5 py-3 font-bold">الهاتف</th>
                        <th class="px-5 py-3 font-bold">عدد الطلبات</th>
                        <th class="px-5 py-3 font-bold">إجمالي الإنفاق</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    @forelse($customers as $row)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3 text-sm font-semibold text-slate-700">{{ $row['name'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600" dir="ltr">{{ $row['phone'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $row['orders'] }}</td>
                            <td class="px-5 py-3 text-sm font-bold text-slate-900">{{ $money($row['total']) }}</td>
                        </tr>
                    @empty
                        <tr><td colspan="4" class="px-5 py-12 text-center text-sm text-slate-400">لا توجد بيانات</td></tr>
                    @endforelse
                </tbody>
            </table>

        @else
            <table class="w-full text-right">
                <thead class="bg-slate-50 text-slate-500 text-xs">
                    <tr>
                        <th class="px-5 py-3 font-bold">الكود</th>
                        <th class="px-5 py-3 font-bold">الوصف</th>
                        <th class="px-5 py-3 font-bold">مرات الاستخدام</th>
                        <th class="px-5 py-3 font-bold">إجمالي الخصم</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    @forelse($coupons as $row)
                        <tr class="hover:bg-slate-50">
                            <td class="px-5 py-3 text-sm font-bold text-slate-800" dir="ltr">{{ $row['code'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $row['title'] }}</td>
                            <td class="px-5 py-3 text-sm text-slate-600">{{ $row['uses'] }}</td>
                            <td class="px-5 py-3 text-sm font-bold text-amber-600">{{ $money($row['discount']) }}</td>
                        </tr>
                    @empty
                        <tr><td colspan="4" class="px-5 py-12 text-center text-sm text-slate-400">لم يُستخدم أي كوبون في هذه الفترة</td></tr>
                    @endforelse
                </tbody>
            </table>
        @endif
    </div>

    @script
    <script>
        const renderReport = () => {
            const el = document.getElementById('reportChart');
            if (!el) return;
            if (window._reportChart) window._reportChart.destroy();

            window._reportChart = new Chart(el, {
                type: 'bar',
                data: {
                    labels: JSON.parse(el.dataset.labels),
                    datasets: [{
                        label: 'المبيعات (د.أ)',
                        data: JSON.parse(el.dataset.totals),
                        backgroundColor: '#C62828',
                        borderRadius: 4,
                    }],
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, ticks: { font: { family: 'Cairo' } } },
                        x: { ticks: { font: { family: 'Cairo' } } },
                    },
                },
            });
        };

        renderReport();
        Livewire.hook('morphed', renderReport);
    </script>
    @endscript
</div>
