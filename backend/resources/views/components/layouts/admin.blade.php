@php
    $admin = auth('admin')->user();
    $sections = \App\Support\AdminNav::for($admin);
    $pendingOrders = $admin?->hasPermission(\App\Support\Permissions::ORDERS_VIEW)
        ? \App\Models\Order::where('status', 'pending')->count()
        : 0;
@endphp
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>{{ $title ?? 'لوحة التحكم' }} | ابن الشام</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Cairo:wght@400;500;600;700;800&display=swap');
        body { font-family: 'Cairo', sans-serif; }
        .nav-active { background: rgba(255,255,255,.14); box-shadow: inset 3px 0 0 #FBBF24; }
        ::-webkit-scrollbar { width: 8px; height: 8px; }
        ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 8px; }
        [x-cloak] { display: none !important; }
    </style>
    @livewireStyles
</head>
<body class="bg-slate-100 text-slate-800">
<div class="flex min-h-screen">

    <!-- Sidebar -->
    <aside class="w-64 shrink-0 bg-gradient-to-b from-red-900 to-red-800 text-white flex flex-col">
        <div class="px-6 py-5 border-b border-white/10">
            <h1 class="text-xl font-extrabold text-amber-300">ابن الشام</h1>
            <p class="text-xs text-red-200 mt-0.5">لوحة الإدارة</p>
        </div>

        <nav class="flex-1 overflow-y-auto px-3 py-4 space-y-5">
            @foreach($sections as $section)
                <div>
                    @if($section['label'])
                        <p class="px-3 mb-1.5 text-[11px] font-bold uppercase tracking-wider text-red-300/70">
                            {{ $section['label'] }}
                        </p>
                    @endif
                    <div class="space-y-0.5">
                        @foreach($section['items'] as $item)
                            @php $isActive = request()->routeIs($item['route']); @endphp
                            <a href="{{ route($item['route']) }}"
                               class="flex items-center justify-between gap-2 px-3 py-2.5 rounded-lg text-sm font-semibold transition
                                      {{ $isActive ? 'nav-active text-white' : 'text-red-100 hover:bg-white/10' }}">
                                <span class="flex items-center gap-2.5">
                                    <x-admin.icon :name="$item['icon']" />
                                    {{ $item['label'] }}
                                </span>
                                @if(($item['badge'] ?? null) === 'pending_orders' && $pendingOrders > 0)
                                    <span class="min-w-[20px] px-1.5 py-0.5 text-[11px] font-bold text-red-900 bg-amber-300 rounded-full text-center">
                                        {{ $pendingOrders }}
                                    </span>
                                @endif
                            </a>
                        @endforeach
                    </div>
                </div>
            @endforeach
        </nav>

        <div class="px-4 py-4 border-t border-white/10">
            <div class="mb-3">
                <p class="text-sm font-bold truncate">{{ $admin?->name }}</p>
                <p class="text-xs text-red-200">{{ $admin?->roleLabel() }}</p>
            </div>
            <form method="POST" action="{{ route('admin.logout') }}">
                @csrf
                <button type="submit"
                        class="w-full px-3 py-2 text-xs font-bold text-red-900 bg-white rounded-lg hover:bg-amber-200 transition">
                    تسجيل الخروج
                </button>
            </form>
        </div>
    </aside>

    <!-- Main -->
    <main class="flex-1 min-w-0 flex flex-col">
        <header class="sticky top-0 z-20 bg-white/90 backdrop-blur border-b border-slate-200 px-8 py-4 flex items-center justify-between">
            <div>
                <h2 class="text-lg font-extrabold text-slate-900">{{ $title ?? 'نظرة عامة' }}</h2>
                <p class="text-xs text-slate-500 mt-0.5">{{ now()->translatedFormat('l، j F Y') }}</p>
            </div>
            <a href="{{ config('app.url') }}" target="_blank"
               class="text-xs font-semibold text-slate-500 hover:text-red-700 transition">
                عرض الموقع ↗
            </a>
        </header>

        @if(session('message'))
            <div class="mx-8 mt-5 px-4 py-3 text-sm font-semibold text-emerald-800 bg-emerald-50 border border-emerald-200 rounded-lg">
                {{ session('message') }}
            </div>
        @endif
        @if(session('error'))
            <div class="mx-8 mt-5 px-4 py-3 text-sm font-semibold text-red-800 bg-red-50 border border-red-200 rounded-lg">
                {{ session('error') }}
            </div>
        @endif

        <div class="flex-1 p-8">
            {{ $slot }}
        </div>
    </main>
</div>

@livewireScripts
</body>
</html>
