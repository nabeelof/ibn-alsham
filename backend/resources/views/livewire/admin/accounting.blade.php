@php
    $money = fn ($v) => number_format((float) $v, 3) . ' د.أ';
    $grossSales = $summary['subtotal'] + $summary['delivery'];
@endphp

<div>
    <x-admin.range-filter :ranges="$ranges" :range="$range" :show-custom="true">
        <span class="text-xs font-semibold text-slate-400 self-center">{{ $periodLabel }}</span>
    </x-admin.range-filter>

    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <x-admin.stat label="صافي المبيعات" :value="$money($summary['revenue'])" tone="green" icon="wallet"
                      :sub="$summary['orders_count'] . ' طلب'" />
        <x-admin.stat label="تكلفة البضاعة المباعة" :value="$money($summary['cogs'])" tone="amber" icon="food" />
        <x-admin.stat label="مجمل الربح" :value="$money($summary['gross_profit'])" tone="blue" icon="chart" />
        <x-admin.stat label="صافي الربح"
                      :value="$money($summary['net_profit'])"
                      :tone="$summary['net_profit'] >= 0 ? 'green' : 'red'"
                      :sub="'هامش صافي ' . number_format($summary['margin'], 1) . '%'"
                      icon="calculator" />
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
        <!-- Profit & loss statement -->
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-3.5 border-b border-slate-100 bg-slate-50">
                <h3 class="font-bold text-slate-800">قائمة الأرباح والخسائر</h3>
            </div>
            <div class="divide-y divide-slate-100">
                <div class="flex justify-between px-5 py-3">
                    <span class="text-sm text-slate-600">إجمالي المبيعات (قبل الخصم)</span>
                    <span class="text-sm font-bold text-slate-900">{{ $money($grossSales) }}</span>
                </div>
                <div class="flex justify-between px-5 py-3">
                    <span class="text-sm text-slate-600">( − ) الخصومات والكوبونات</span>
                    <span class="text-sm font-bold text-amber-600">{{ $money($summary['discounts']) }}</span>
                </div>
                <div class="flex justify-between px-5 py-3 bg-slate-50">
                    <span class="text-sm font-bold text-slate-700">صافي المبيعات</span>
                    <span class="text-sm font-extrabold text-slate-900">{{ $money($summary['revenue']) }}</span>
                </div>
                <div class="flex justify-between px-5 py-3">
                    <span class="text-sm text-slate-600">( − ) تكلفة البضاعة المباعة</span>
                    <span class="text-sm font-bold text-amber-600">{{ $money($summary['cogs']) }}</span>
                </div>
                <div class="flex justify-between px-5 py-3 bg-slate-50">
                    <span class="text-sm font-bold text-slate-700">مجمل الربح</span>
                    <span class="text-sm font-extrabold text-blue-600">{{ $money($summary['gross_profit']) }}</span>
                </div>
                <div class="flex justify-between px-5 py-3">
                    <span class="text-sm text-slate-600">( − ) المصروفات التشغيلية</span>
                    <span class="text-sm font-bold text-red-600">{{ $money($summary['expenses']) }}</span>
                </div>
                <div class="flex justify-between px-5 py-4 bg-slate-900">
                    <span class="text-sm font-extrabold text-white">صافي الربح</span>
                    <span class="text-base font-extrabold {{ $summary['net_profit'] >= 0 ? 'text-emerald-400' : 'text-red-400' }}">
                        {{ $money($summary['net_profit']) }}
                    </span>
                </div>
            </div>
            @if($summary['cogs'] <= 0)
                <p class="px-5 py-3 text-[11px] text-slate-400 bg-amber-50 border-t border-amber-100">
                    ملاحظة: تكلفة البضاعة تُحتسب من "سعر التكلفة" لكل منتج. أضِف سعر التكلفة من صفحة المنتجات ليصبح الربح دقيقاً.
                </p>
            @endif
        </div>

        <!-- Expenses by category -->
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <div class="px-5 py-3.5 border-b border-slate-100 bg-slate-50 flex items-center justify-between">
                <h3 class="font-bold text-slate-800">المصروفات حسب البند</h3>
                <a href="{{ route('admin.expenses') }}" class="text-xs font-bold text-red-700 hover:underline">إدارة المصروفات</a>
            </div>
            <div class="divide-y divide-slate-100">
                @forelse($expenseCategories as $row)
                    <div class="flex items-center justify-between px-5 py-3">
                        <span class="flex items-center gap-2.5 text-sm text-slate-700">
                            <span class="w-2.5 h-2.5 rounded-full" style="background: {{ $row['color'] }}"></span>
                            {{ $row['name'] }}
                        </span>
                        <span class="text-sm font-bold text-slate-900">{{ $money($row['total']) }}</span>
                    </div>
                @empty
                    <p class="px-5 py-12 text-center text-sm text-slate-400">لا توجد مصروفات مسجّلة في هذه الفترة</p>
                @endforelse
            </div>

            <div class="px-5 py-3.5 border-t border-slate-100 bg-slate-50">
                <h3 class="font-bold text-slate-800 text-sm">التحصيل حسب وسيلة الدفع</h3>
            </div>
            <div class="divide-y divide-slate-100">
                @forelse($payments as $row)
                    <div class="flex items-center justify-between px-5 py-3">
                        <span class="text-sm text-slate-700">{{ $row['label'] }}</span>
                        <span class="text-sm font-bold text-slate-900">{{ $money($row['total']) }}</span>
                    </div>
                @empty
                    <p class="px-5 py-8 text-center text-sm text-slate-400">لا توجد مبالغ محصّلة</p>
                @endforelse
            </div>
        </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 p-5">
        <h3 class="font-bold text-slate-800 mb-4">تطوّر المبيعات خلال الفترة</h3>
        <canvas id="accountingChart" height="80"
                data-labels='@json(array_column($daily, "label"))'
                data-totals='@json(array_column($daily, "total"))'></canvas>
    </div>

    @script
    <script>
        const renderAccounting = () => {
            const el = document.getElementById('accountingChart');
            if (!el) return;
            if (window._acctChart) window._acctChart.destroy();

            window._acctChart = new Chart(el, {
                type: 'line',
                data: {
                    labels: JSON.parse(el.dataset.labels),
                    datasets: [{
                        label: 'المبيعات (د.أ)',
                        data: JSON.parse(el.dataset.totals),
                        borderColor: '#059669',
                        backgroundColor: 'rgba(5,150,105,.10)',
                        fill: true,
                        tension: .35,
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

        renderAccounting();
        Livewire.hook('morphed', renderAccounting);
    </script>
    @endscript
</div>
