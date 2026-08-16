<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\UserAddress;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AddressController extends Controller
{
    use ApiResponse;

    public function index(Request $request): JsonResponse
    {
        $addresses = $request->user()->addresses()
            ->orderByDesc('is_default')
            ->orderByDesc('created_at')
            ->get()
            ->map(fn ($a) => $this->formatAddress($a));

        return $this->success($addresses);
    }

    public function create(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'address' => 'required|string|max:255',
            'city' => 'nullable|string|max:100',
            'area' => 'nullable|string|max:100',
            'lat' => 'nullable|numeric',
            'lng' => 'nullable|numeric',
            'is_default' => 'nullable|boolean',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        if ($request->boolean('is_default')) {
            $request->user()->addresses()->update(['is_default' => false]);
        }

        $address = UserAddress::create([
            'user_id' => $request->user()->id,
            'address' => $request->input('address'),
            'city' => $request->input('city'),
            'area' => $request->input('area'),
            'lat' => $request->input('lat'),
            'lng' => $request->input('lng'),
            'is_default' => $request->boolean('is_default'),
        ]);

        return $this->success($this->formatAddress($address));
    }

    public function update(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:user_addresses,id',
            'address' => 'required|string|max:255',
            'city' => 'nullable|string|max:100',
            'area' => 'nullable|string|max:100',
            'lat' => 'nullable|numeric',
            'lng' => 'nullable|numeric',
            'is_default' => 'nullable|boolean',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $address = UserAddress::where('user_id', $request->user()->id)
            ->findOrFail($request->input('id'));

        if ($request->boolean('is_default')) {
            $request->user()->addresses()->update(['is_default' => false]);
        }

        $address->update([
            'address' => $request->input('address'),
            'city' => $request->input('city'),
            'area' => $request->input('area'),
            'lat' => $request->input('lat'),
            'lng' => $request->input('lng'),
            'is_default' => $request->boolean('is_default'),
        ]);

        return $this->success($this->formatAddress($address));
    }

    public function updateLocation(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:user_addresses,id',
            'lat' => 'required|numeric',
            'lng' => 'required|numeric',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $address = UserAddress::where('user_id', $request->user()->id)
            ->findOrFail($request->input('id'));

        $address->update([
            'lat' => $request->input('lat'),
            'lng' => $request->input('lng'),
        ]);

        return $this->success($this->formatAddress($address));
    }

    public function makeDefault(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:user_addresses,id',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $request->user()->addresses()->update(['is_default' => false]);
        $address = UserAddress::where('user_id', $request->user()->id)
            ->findOrFail($request->input('id'));
        $address->update(['is_default' => true]);

        return $this->success($this->formatAddress($address));
    }

    public function delete(int $id, Request $request): JsonResponse
    {
        $address = UserAddress::where('user_id', $request->user()->id)
            ->findOrFail($id);
        $address->delete();

        return $this->success(['deleted' => true]);
    }

    public function homeDelivery(Request $request): JsonResponse
    {
        $address = $request->user()->addresses()->where('is_default', true)->first()
            ?? $request->user()->addresses()->first();

        return $this->success($address ? $this->formatAddress($address) : null);
    }

    public function countries(): JsonResponse
    {
        return $this->success([]);
    }

    public function statesByCountry(): JsonResponse
    {
        return $this->success([]);
    }

    public function citiesByState(): JsonResponse
    {
        return $this->success([]);
    }

    public function citiesByCountry(): JsonResponse
    {
        return $this->success([]);
    }

    public function areasByCity(): JsonResponse
    {
        return $this->success([]);
    }

    public function updateAddressInCart(): JsonResponse
    {
        return $this->success(['updated' => true]);
    }

    public function updateShippingTypeInCart(): JsonResponse
    {
        return $this->success(['updated' => true]);
    }

    private function formatAddress(UserAddress $address): array
    {
        return [
            'id' => $address->id,
            'address' => $address->address,
            'city' => $address->city,
            'area' => $address->area,
            'lat' => $address->lat,
            'lng' => $address->lng,
            'is_default' => $address->is_default,
        ];
    }
}
