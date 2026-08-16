<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class CheckAdminRole
{
    /**
     * Handle an incoming request.
     * Usage: middleware('admin.role:content') or middleware('admin.role:orders')
     */
    public function handle(Request $request, Closure $next, string $area): Response
    {
        $admin = $request->user('admin');

        if (!$admin || !$admin->is_active) {
            abort(403, 'Access denied');
        }

        // Super admin can access everything
        if ($admin->isSuperAdmin()) {
            return $next($request);
        }

        $allowed = match ($area) {
            'content' => $admin->canManageContent(),
            'orders' => $admin->canManageOrders(),
            'all' => $admin->isSuperAdmin(),
            default => false,
        };

        if (!$allowed) {
            abort(403, 'You do not have permission to access this area');
        }

        return $next($request);
    }
}
