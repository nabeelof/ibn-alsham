<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class SetLocale
{
    public function handle(Request $request, Closure $next)
    {
        $header = $request->header('Accept-Language', 'ar');
        // Extract base locale (first two chars)
        $locale = substr($header, 0, 2);

        if (in_array($locale, ['ar', 'en'])) {
            app()->setLocale($locale);
        } else {
            app()->setLocale('ar'); // Fallback to ar for Damascene branding
        }

        return $next($request);
    }
}
