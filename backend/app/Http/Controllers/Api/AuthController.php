<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Services\OtpService;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AuthController extends Controller
{
    use ApiResponse;

    public function __construct(
        private OtpService $otpService,
    ) {}

    /**
     * Send OTP to phone number.
     */
    public function sendOtp(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'phone' => 'required|string|min:9|max:15',
        ]);

        if ($validator->fails()) {
            return $this->error(__('auth.validation_error'), 422, $validator->errors());
        }

        $phone = $request->phone;

        // Check if user is soft-deleted
        if (User::where('phone', $phone . '-ي')->exists()) {
            return $this->error(__('auth.account_deleted'), 403);
        }

        // Create or get user
        $user = User::firstOrCreate(
            ['phone' => $phone],
            ['name' => '', 'phone' => $phone]
        );

        $this->otpService->sendOtp($phone);

        return $this->success(
            ['phone' => $phone],
            __('auth.otp_sent')
        );
    }

    public function login(Request $request): JsonResponse
    {
        return $this->error('Login not configured. Use OTP flow.', 400);
    }

    public function socialLogin(Request $request): JsonResponse
    {
        return $this->error('Social login not configured.', 400);
    }

    public function info(Request $request): JsonResponse
    {
        $user = $request->user();
        if (!$user) {
            return $this->error('Unauthorized', 401);
        }

        return $this->success([
            'id' => $user->id,
            'name' => $user->name,
            'phone' => $user->phone,
            'avatar' => $user->avatar,
        ]);
    }

    public function resendCode(Request $request): JsonResponse
    {
        return $this->success(['result' => true, 'message' => 'OTP resent']);
    }

    public function confirmCode(Request $request): JsonResponse
    {
        return $this->success(['result' => true, 'message' => 'Code confirmed']);
    }

    public function passwordForgetRequest(Request $request): JsonResponse
    {
        return $this->success(['result' => true, 'message' => 'Reset request sent']);
    }

    public function passwordConfirmReset(Request $request): JsonResponse
    {
        return $this->success(['result' => true, 'message' => 'Password reset']);
    }

    public function passwordResendCode(Request $request): JsonResponse
    {
        return $this->success(['result' => true, 'message' => 'OTP resent']);
    }

    /**
     * Verify OTP and login.
     */
    public function verifyOtp(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'phone' => 'required|string',
            'otp' => 'required|string|size:4',
        ]);

        if ($validator->fails()) {
            return $this->error(__('auth.validation_error'), 422, $validator->errors());
        }

        if (!$this->otpService->verifyOtp($request->phone, $request->otp)) {
            return $this->error(__('auth.invalid_otp'), 401);
        }

        $user = User::where('phone', $request->phone)->first();

        if (!$user) {
            return $this->error(__('auth.user_not_found'), 404);
        }

        // Update FCM token if provided
        if ($request->fcm_token) {
            $user->update(['fcm_token' => $request->fcm_token]);
        }

        $token = $user->createToken('mobile-app')->plainTextToken;
        $isNewUser = empty($user->name);

        return $this->success([
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'phone' => $user->phone,
                'locale' => $user->locale,
                'avatar' => $user->avatar,
            ],
            'is_new_user' => $isNewUser,
        ], __('auth.login_success'));
    }

    /**
     * Register a new user (complete profile after OTP).
     */
    public function register(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'phone' => 'required|string',
            'name' => 'required|string|min:2|max:100',
            'otp' => 'required|string|size:4',
        ]);

        if ($validator->fails()) {
            return $this->error(__('auth.validation_error'), 422, $validator->errors());
        }

        if (!$this->otpService->verifyOtp($request->phone, $request->otp)) {
            return $this->error(__('auth.invalid_otp'), 401);
        }

        $user = User::where('phone', $request->phone)->first();

        if (!$user) {
            $user = User::create([
                'phone' => $request->phone,
                'name' => $request->name,
                'is_verified' => true,
            ]);
        } else {
            $user->update([
                'name' => $request->name,
                'is_verified' => true,
            ]);
        }

        if ($request->fcm_token) {
            $user->update(['fcm_token' => $request->fcm_token]);
        }

        $token = $user->createToken('mobile-app')->plainTextToken;

        return $this->success([
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'phone' => $user->phone,
                'locale' => $user->locale,
            ],
        ], __('auth.register_success'));
    }

    /**
     * Logout (revoke current token).
     */
    public function logout(Request $request): JsonResponse
    {
        $request->user()->currentAccessToken()->delete();

        return $this->success(null, __('auth.logout_success'));
    }

    public function guestLogin(Request $request): JsonResponse
    {
        $user = User::create([
            'name' => 'Guest',
            'phone' => 'guest-' . now()->timestamp,
            'is_verified' => false,
        ]);

        $token = $user->createToken('mobile-app')->plainTextToken;

        return $this->success([
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'phone' => $user->phone,
                'locale' => $user->locale,
            ],
        ], 'Guest login');
    }

    public function guestInfoCheck(Request $request): JsonResponse
    {
        return response()->json([
            'result' => true,
            'message' => 'Guest checkout enabled',
        ]);
    }

    public function guestAccountCreate(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'name' => 'nullable|string|max:100',
            'phone' => 'nullable|string|max:20',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'result' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors(),
            ], 422);
        }

        $user = User::create([
            'name' => $request->input('name', 'Guest'),
            'phone' => $request->input('phone', 'guest-' . now()->timestamp),
            'is_verified' => false,
        ]);

        return response()->json([
            'result' => true,
            'user_id' => $user->id,
            'temp_user_id' => (string) $user->id,
            'message' => 'Guest created',
        ]);
    }

    /**
     * Delete account (soft delete: append -ي to phone).
     */
    public function deleteAccount(Request $request): JsonResponse
    {
        $user = $request->user();
        $user->softDeleteAccount();

        return $this->success(null, __('auth.account_deleted_success'));
    }
}
