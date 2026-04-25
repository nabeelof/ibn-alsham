<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>{{ $title ?? 'لوحة التحكم' }} | ابن الشام</title>
    <!-- Simple fallback to Tailwind via CDN since node/npm isn't available for build -->
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700&display=swap');
        body { font-family: 'Cairo', sans-serif; background-color: #f9fafb; }
    </style>
    @livewireStyles
</head>
<body class="flex min-h-screen text-gray-900 bg-gray-100">

    <!-- Sidebar -->
    <aside class="w-64 bg-red-800 text-white flex flex-col shadow-lg">
        <div class="p-6 text-center border-b border-red-700">
            <h1 class="text-2xl font-bold text-yellow-400">ابن الشام</h1>
            <p class="text-sm text-red-200 mt-1">لوحة الإدارة</p>
        </div>
        <nav class="flex-1 px-4 py-6 space-y-2">
            <a href="{{ route('admin.dashboard') }}" class="flex items-center px-4 py-3 bg-red-700 rounded-lg hover:bg-red-600 transition">
                <span class="font-semibold">الرئيسية</span>
            </a>
            <a href="{{ route('admin.orders') }}" class="flex items-center px-4 py-3 rounded-lg hover:bg-red-700 transition">
                <span class="font-semibold">الطلبات</span>
            </a>
            <a href="{{ route('admin.menu') }}" class="flex items-center px-4 py-3 rounded-lg hover:bg-red-700 transition">
                <span class="font-semibold">قائمة الطعام</span>
            </a>
            <a href="{{ route('admin.categories') }}" class="flex items-center px-4 py-3 rounded-lg hover:bg-red-700 transition">
                <span class="font-semibold">الأقسام</span>
            </a>
        </nav>
        <div class="p-4 border-t border-red-700">
            <form method="POST" action="{{ route('admin.logout') }}">
                @csrf
                <button type="submit" class="w-full px-4 py-2 text-sm font-bold text-red-800 bg-white rounded-lg hover:bg-gray-100 transition">
                    تسجيل الخروج
                </button>
            </form>
        </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col">
        <!-- Top bar -->
        <header class="bg-white shadow px-8 py-4 flex items-center justify-between">
            <h2 class="text-xl font-semibold text-gray-800">
                {{ $title ?? 'نظرة عامة' }}
            </h2>
            <div class="text-sm font-medium text-gray-500">
                مرحباً بمشرف ابن الشام
            </div>
        </header>

        <!-- Page Content -->
        <div class="flex-1 p-8 overflow-auto">
            {{ $slot }}
        </div>
    </main>

    @livewireScripts
</body>
</html>
