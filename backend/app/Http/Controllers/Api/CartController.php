<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Cart;
use App\Models\CartItem;
use App\Models\Product;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class CartController extends Controller
{
    use ApiResponse;

    public function index(Request $request): JsonResponse
    {
        $cart = $this->resolveCart($request);
        if (!$cart) {
            return $this->error('Missing user_id or temp_user_id', 422);
        }

        return $this->success($this->formatCartResponse($cart));
    }

    public function count(Request $request): JsonResponse
    {
        $cart = $this->resolveCart($request);
        if (!$cart) {
            return response()->json(['status' => false, 'count' => 0]);
        }

        $count = $cart->items()->sum('quantity');
        return response()->json(['status' => true, 'count' => $count]);
    }

    public function add(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:products,id',
            'quantity' => 'required|integer|min:1',
            'variant' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $cart = $this->resolveCart($request);
        if (!$cart) {
            return $this->error('Missing user_id or temp_user_id', 422);
        }

        $product = Product::findOrFail($request->input('id'));
        $variant = $request->input('variant');

        $item = $cart->items()
            ->where('product_id', $product->id)
            ->when($variant, fn ($q) => $q->where('variant', $variant), fn ($q) => $q->whereNull('variant'))
            ->first();

        if ($item) {
            $item->quantity += (int) $request->input('quantity');
        } else {
            $item = new CartItem([
                'product_id' => $product->id,
                'quantity' => (int) $request->input('quantity'),
                'variant' => $variant,
                'unit_price' => $product->price,
                'discount_price' => $product->discounted_price,
                'options_total' => 0,
            ]);
            $cart->items()->save($item);
        }

        $item->total = $this->calculateItemTotal($item);
        $item->save();

        return $this->success($this->formatCartResponse($cart->fresh('items.product')));
    }

    public function remove(int $id, Request $request): JsonResponse
    {
        $cart = $this->resolveCart($request, false);
        if (!$cart && !$request->user()) {
            return $this->error('Unauthorized', 401);
        }

        $item = CartItem::findOrFail($id);
        if ($request->user()) {
            if ($item->cart?->user_id !== $request->user()->id) {
                return $this->error('Forbidden', 403);
            }
        }
        $item->delete();

        return $this->success(['deleted' => true]);
    }

    public function process(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'cart_ids' => 'required|string',
            'cart_quantities' => 'required|string',
        ]);

        if ($validator->fails()) {
            return $this->error('Validation error', 422, $validator->errors());
        }

        $cart = $this->resolveCart($request);
        if (!$cart) {
            return $this->error('Missing user_id or temp_user_id', 422);
        }

        $ids = array_filter(explode(',', $request->input('cart_ids')));
        $quantities = array_map('intval', array_filter(explode(',', $request->input('cart_quantities'))));

        foreach ($ids as $index => $id) {
            $qty = $quantities[$index] ?? 0;
            $item = $cart->items()->where('id', $id)->first();
            if (!$item) {
                continue;
            }
            if ($qty <= 0) {
                $item->delete();
                continue;
            }
            $item->quantity = $qty;
            $item->total = $this->calculateItemTotal($item);
            $item->save();
        }

        return $this->success($this->formatCartResponse($cart->fresh('items.product')));
    }

    public function summary(Request $request): JsonResponse
    {
        $cart = $this->resolveCart($request);
        if (!$cart) {
            return response()->json([
                'sub_total' => '0.000',
                'tax' => '0.000',
                'shipping_cost' => '0.000',
                'discount' => '0.000',
                'grand_total' => '0.000',
                'grand_total_value' => 0.0,
                'coupon_code' => null,
                'coupon_applied' => false,
            ]);
        }

        $totals = $this->calculateTotals($cart);
        return response()->json([
            'sub_total' => number_format($totals['subtotal'], 3, '.', ''),
            'tax' => number_format(0, 3, '.', ''),
            'shipping_cost' => number_format(0, 3, '.', ''),
            'discount' => number_format(0, 3, '.', ''),
            'grand_total' => number_format($totals['grand_total'], 3, '.', ''),
            'grand_total_value' => (float) $totals['grand_total'],
            'coupon_code' => null,
            'coupon_applied' => false,
        ]);
    }

    private function resolveCart(Request $request, bool $createIfMissing = true): ?Cart
    {
        $user = $request->user();
        $userId = $user?->id ?? $request->input('user_id');
        $tempUserId = $request->input('temp_user_id');

        if ($userId) {
            return $createIfMissing
                ? Cart::firstOrCreate(['user_id' => $userId], ['temp_user_id' => null])
                : Cart::where('user_id', $userId)->first();
        }

        if ($tempUserId) {
            return $createIfMissing
                ? Cart::firstOrCreate(['temp_user_id' => $tempUserId], ['user_id' => null])
                : Cart::where('temp_user_id', $tempUserId)->first();
        }

        return null;
    }

    private function calculateItemTotal(CartItem $item): float
    {
        $unit = $item->discount_price ?? $item->unit_price;
        return ((float) $unit * (int) $item->quantity) + (float) $item->options_total;
    }

    private function calculateTotals(Cart $cart): array
    {
        $subtotal = 0.0;
        foreach ($cart->items as $item) {
            $subtotal += (float) $this->calculateItemTotal($item);
        }

        return [
            'subtotal' => $subtotal,
            'grand_total' => $subtotal,
        ];
    }

    private function formatCartResponse(Cart $cart): array
    {
        $cart->loadMissing(['items.product']);
        $totals = $this->calculateTotals($cart);

        $items = $cart->items->map(fn ($item) => [
            'id' => $item->id,
            'owner_id' => null,
            'user_id' => $cart->user_id,
            'product_id' => $item->product_id,
            'product_name' => $item->product?->name,
            'auction_product' => 0,
            'product_thumbnail_image' => $item->product?->image_url,
            'variation' => $item->variant,
            'price' => number_format((float) ($item->discount_price ?? $item->unit_price), 3, '.', ''),
            'currency_symbol' => 'JOD',
            'tax' => '0.000',
            'shipping_cost' => 0,
            'quantity' => $item->quantity,
            'lower_limit' => 1,
            'upper_limit' => 99,
            'digital' => 0,
            'stock' => 999,
        ])->values();

        return [
            'grand_total' => number_format($totals['grand_total'], 3, '.', ''),
            'data' => [
                [
                    'name' => 'IbnAlsham',
                    'owner_id' => null,
                    'sub_total' => number_format($totals['subtotal'], 3, '.', ''),
                    'cart_items' => $items,
                ],
            ],
        ];
    }
}
