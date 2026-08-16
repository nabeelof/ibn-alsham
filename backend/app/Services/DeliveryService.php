<?php

namespace App\Services;

use App\Models\DeliveryZone;

class DeliveryService
{
    /**
     * Calculate delivery fee based on distance in kilometers.
     *
     * Pricing tiers:
     * - ≤ 1 km → 1.000 JOD (flat)
     * - 1–5 km → 0.500 JOD per km
     * - > 5 km → 0.400 JOD per km
     */
    public function calculateDeliveryFee(float $distanceKm): float
    {
        // Try database zones first
        $zones = DeliveryZone::active()->orderBy('min_km')->get();

        if ($zones->isNotEmpty()) {
            return $this->calculateFromZones($zones, $distanceKm);
        }

        // Fallback to hardcoded tiers
        return $this->calculateDefault($distanceKm);
    }

    private function calculateFromZones($zones, float $distanceKm): float
    {
        $totalFee = 0;

        foreach ($zones as $zone) {
            if ($distanceKm <= $zone->min_km) break;

            $kmInZone = min($distanceKm, $zone->max_km) - $zone->min_km;
            if ($kmInZone <= 0) continue;

            $totalFee += $zone->base_price + ($kmInZone * $zone->price_per_km);
        }

        return round($totalFee, 3);
    }

    private function calculateDefault(float $distanceKm): float
    {
        if ($distanceKm <= 1) {
            return 1.000;
        }

        if ($distanceKm <= 5) {
            return round($distanceKm * 0.500, 3);
        }

        // First 5 km at 0.50, rest at 0.40
        $first5 = 5 * 0.500;
        $remaining = ($distanceKm - 5) * 0.400;

        return round($first5 + $remaining, 3);
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     */
    public function calculateDistance(float $lat1, float $lng1, float $lat2, float $lng2): float
    {
        $earthRadius = 6371; // km

        $dLat = deg2rad($lat2 - $lat1);
        $dLng = deg2rad($lng2 - $lng1);

        $a = sin($dLat / 2) * sin($dLat / 2) +
             cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
             sin($dLng / 2) * sin($dLng / 2);

        $c = 2 * atan2(sqrt($a), sqrt(1 - $a));

        return round($earthRadius * $c, 2);
    }
}
