<?php

namespace App\Services;

use App\Models\User;
use Carbon\Carbon;

class OtpService
{
    /**
     * Generate and store OTP for a phone number.
     * In test mode, always returns 1234.
     */
    public function sendOtp(string $phone): string
    {
        $otp = $this->generateOtp();

        $user = User::where('phone', $phone)->first();

        if ($user) {
            $user->update([
                'otp' => $otp,
                'otp_expires_at' => Carbon::now()->addMinutes(
                    (int) config('app.otp_expiry_minutes', 5)
                ),
            ]);
        }

        // In production, integrate SMS gateway here
        // SmsGateway::send($phone, "Your IBN ALSHAM code: {$otp}");

        return $otp;
    }

    /**
     * Verify OTP for a phone number.
     */
    public function verifyOtp(string $phone, string $otp): bool
    {
        // Test code always works
        if ($otp === config('app.otp_test_code', '1234')) {
            return true;
        }

        $user = User::where('phone', $phone)->first();

        if (!$user) return false;
        if ($user->otp !== $otp) return false;
        if ($user->otp_expires_at && $user->otp_expires_at->isPast()) return false;

        // Clear OTP after successful verification
        $user->update([
            'otp' => null,
            'otp_expires_at' => null,
            'is_verified' => true,
        ]);

        return true;
    }

    private function generateOtp(): string
    {
        // In test/local mode, always return test code
        if (app()->environment('local', 'testing')) {
            return config('app.otp_test_code', '1234');
        }

        return str_pad((string) random_int(0, 9999), 4, '0', STR_PAD_LEFT);
    }
}
