<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>لوحة التحكم | ابن الشام</title>
    <!-- Simple fallback to Tailwind via CDN since node/npm isn't available for build -->
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700&display=swap');
        body { font-family: 'Cairo', sans-serif; background-color: #f3f4f6; }
    </style>
</head>
<body class="flex items-center justify-center min-h-screen">

    <div class="w-full max-w-md p-8 space-y-6 bg-white shadow-xl rounded-2xl">
        <div class="text-center">
            <h1 class="text-3xl font-bold text-red-700">ابن الشام</h1>
            <p class="mt-2 text-sm text-gray-600">تسجيل الدخول للإدارة</p>
        </div>

        @if($errors->any())
            <div class="p-4 text-sm text-red-700 bg-red-100 rounded-lg">
                {{ $errors->first() }}
            </div>
        @endif

        <form method="POST" action="{{ route('admin.login.submit') }}" class="space-y-4">
            @csrf
            <div>
                <label for="email" class="block text-sm font-medium text-gray-700">البريد الإلكتروني</label>
                <input id="email" name="email" type="email" required value="{{ old('email') }}" class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500">
            </div>

            <div>
                <label for="password" class="block text-sm font-medium text-gray-700">كلمة المرور</label>
                <input id="password" name="password" type="password" required class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500">
            </div>

            <div class="flex items-center">
                <input id="remember" name="remember" type="checkbox" class="w-4 h-4 text-red-600 border-gray-300 rounded focus:ring-red-500">
                <label for="remember" class="block ml-2 text-sm text-gray-900 mr-2">
                    تذكرني
                </label>
            </div>

            <div>
                <button type="submit" class="flex justify-center w-full px-4 py-2 text-sm font-medium text-white bg-red-700 border border-transparent rounded-md shadow-sm hover:bg-red-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500">
                    دخول
                </button>
            </div>
        </form>
    </div>

</body>
</html>
