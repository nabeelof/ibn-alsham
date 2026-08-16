<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ProfileController extends Controller
{
    use ApiResponse;

    public function show(Request $request)
    {
        $user = $request->user();

        return $this->success([
            'id' => $user->id,
            'name' => $user->name,
            'phone' => $user->phone,
            'fcm_token' => $user->fcm_token,
        ]);
    }

    public function update(Request $request)
    {
        $user = $request->user();

        $validated = $request->validate([
            'name' => 'sometimes|string|min:2|max:100',
            'fcm_token' => 'sometimes|string',
        ]);

        $user->update($validated);

        return $this->success([
            'id' => $user->id,
            'name' => $user->name,
            'phone' => $user->phone,
        ], message: 'تم تحديث الملف الشخصي بنجاح');
    }

    public function updateDeviceToken(Request $request)
    {
        $user = $request->user();
        $request->validate([
            'device_token' => 'required|string',
        ]);

        $user->update(['fcm_token' => $request->input('device_token')]);

        return $this->success(['result' => true, 'message' => 'Device token updated']);
    }

    public function updateImage(Request $request)
    {
        $user = $request->user();
        $request->validate([
            'image' => 'required|image|max:2048',
        ]);

        $path = $request->file('image')->store('avatars', 'public');
        $user->update(['avatar' => $path]);

        return $this->success([
            'result' => true,
            'path' => asset("storage/{$path}"),
        ]);
    }

    public function counters(Request $request)
    {
        $user = $request->user();
        return $this->success([
            'cart_item_count' => $user->cart?->items()->sum('quantity') ?? 0,
            'wishlist_item_count' => $user->favorites()->count(),
            'orders_count' => $user->orders()->count(),
        ]);
    }

    public function checkPhoneAndEmail(Request $request)
    {
        return $this->success(['result' => true]);
    }

    public function customerInfo(Request $request)
    {
        $user = $request->user();
        return $this->success([
            'id' => $user->id,
            'name' => $user->name,
            'phone' => $user->phone,
            'avatar' => $user->avatar ? asset("storage/{$user->avatar}") : null,
        ]);
    }

    public function deleteAccount(Request $request)
    {
        $user = $request->user();

        // Soft delete: append يَ to the phone number
        $user->update([
            'phone' => $user->phone . '-' . now()->timestamp,
            'name' => 'حساب محذوف',
            'fcm_token' => null,
        ]);

        // Revoke all tokens
        $user->tokens()->delete();

        return $this->success(message: 'تم حذف الحساب بنجاح');
    }
}
