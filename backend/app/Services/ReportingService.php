<?php

namespace App\Services;

use App\Models\Expense;
use App\Models\Order;
use App\Models\OrderItem;
use Carbon\CarbonInterface;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;

/**
 * Shared metric queries behind the dashboard, reports and accounting screens.
 *
 * Only orders that actually earned money count as revenue — pending and
 * cancelled orders are excluded everywhere so the figures reconcile.
 */
class ReportingService
{
    /** Statuses that represent money earned. */
    public const REVENUE_STATUSES = ['delivered', 'completed'];

    /**
     * Columns are table-qualified because callers join this query onto users
     * and order_items, which carry the same column names.
     */
    public function revenueQuery(CarbonInterface $from, CarbonInterface $to)
    {
        return Order::whereIn('orders.status', self::REVENUE_STATUSES)
            ->whereBetween('orders.created_at', [$from->copy()->startOfDay(), $to->copy()->endOfDay()]);
    }

    /** Headline totals for a period. */
    public function summary(CarbonInterface $from, CarbonInterface $to): array
    {
        $orders = $this->revenueQuery($from, $to);

        $totals = $orders->clone()
            ->selectRaw('COUNT(*) as orders_count')
            ->selectRaw('COALESCE(SUM(orders.subtotal),0) as subtotal')
            ->selectRaw('COALESCE(SUM(orders.discount_amount),0) as discounts')
            ->selectRaw('COALESCE(SUM(orders.delivery_fee),0) as delivery')
            ->selectRaw('COALESCE(SUM(orders.total),0) as total')
            ->first();

        $cogs = (float) OrderItem::whereIn('order_id', $orders->clone()->select('orders.id'))
            ->selectRaw('COALESCE(SUM(unit_cost * quantity),0) as cogs')
            ->value('cogs');

        $expenses = (float) Expense::between($from->copy()->startOfDay(), $to->copy()->endOfDay())
            ->sum('amount');

        $revenue = (float) $totals->total;
        $grossProfit = $revenue - $cogs;

        return [
            'orders_count' => (int) $totals->orders_count,
            'subtotal' => (float) $totals->subtotal,
            'discounts' => (float) $totals->discounts,
            'delivery' => (float) $totals->delivery,
            'revenue' => $revenue,
            'cogs' => $cogs,
            'gross_profit' => $grossProfit,
            'expenses' => $expenses,
            'net_profit' => $grossProfit - $expenses,
            'average_order' => $totals->orders_count > 0 ? $revenue / $totals->orders_count : 0.0,
            'margin' => $revenue > 0 ? ($grossProfit - $expenses) / $revenue * 100 : 0.0,
        ];
    }

    /** Revenue per day, zero-filled so the chart has no gaps. */
    public function dailyRevenue(CarbonInterface $from, CarbonInterface $to): array
    {
        $rows = $this->revenueQuery($from, $to)
            ->selectRaw('DATE(orders.created_at) as day, COUNT(*) as orders_count, COALESCE(SUM(orders.total),0) as total')
            ->groupBy('day')
            ->pluck('total', 'day');

        $counts = $this->revenueQuery($from, $to)
            ->selectRaw('DATE(orders.created_at) as day, COUNT(*) as c')
            ->groupBy('day')
            ->pluck('c', 'day');

        $series = [];
        for ($day = $from->copy()->startOfDay(); $day->lte($to); $day->addDay()) {
            $key = $day->toDateString();
            $series[] = [
                'day' => $key,
                'label' => $day->translatedFormat('j M'),
                'total' => (float) ($rows[$key] ?? 0),
                'orders' => (int) ($counts[$key] ?? 0),
            ];
        }

        return $series;
    }

    /** Best-selling products by quantity within the period. */
    public function topProducts(CarbonInterface $from, CarbonInterface $to, int $limit = 10): array
    {
        return OrderItem::whereIn('order_id', $this->revenueQuery($from, $to)->select('orders.id'))
            ->selectRaw('product_name_ar as name')
            ->selectRaw('SUM(quantity) as qty')
            ->selectRaw('SUM(total) as revenue')
            ->selectRaw('COALESCE(SUM(unit_cost * quantity),0) as cost')
            ->groupBy('product_name_ar')
            ->orderByDesc('qty')
            ->limit($limit)
            ->get()
            ->map(fn ($row) => [
                'name' => $row->name,
                'qty' => (int) $row->qty,
                'revenue' => (float) $row->revenue,
                'profit' => (float) $row->revenue - (float) $row->cost,
            ])
            ->all();
    }

    /** Revenue split by product category. */
    public function salesByCategory(CarbonInterface $from, CarbonInterface $to): array
    {
        return OrderItem::whereIn('order_items.order_id', $this->revenueQuery($from, $to)->select('orders.id'))
            ->join('products', 'products.id', '=', 'order_items.product_id')
            ->join('categories', 'categories.id', '=', 'products.category_id')
            ->selectRaw('categories.name_ar as name')
            ->selectRaw('SUM(order_items.total) as revenue')
            ->selectRaw('SUM(order_items.quantity) as qty')
            ->groupBy('categories.name_ar')
            ->orderByDesc('revenue')
            ->get()
            ->map(fn ($row) => [
                'name' => $row->name,
                'revenue' => (float) $row->revenue,
                'qty' => (int) $row->qty,
            ])
            ->all();
    }

    /** Delivery vs pick-up split. */
    public function orderTypeSplit(CarbonInterface $from, CarbonInterface $to): array
    {
        return $this->revenueQuery($from, $to)
            ->selectRaw('orders.type as type, COUNT(*) as orders_count, COALESCE(SUM(orders.total),0) as total')
            ->groupBy('orders.type')
            ->get()
            ->map(fn ($row) => [
                'type' => $row->type,
                'label' => $row->type === 'delivery' ? 'توصيل' : 'استلام',
                'orders' => (int) $row->orders_count,
                'total' => (float) $row->total,
            ])
            ->all();
    }

    /** Money collected per payment method — the cash-vs-CliQ reconciliation. */
    public function paymentBreakdown(CarbonInterface $from, CarbonInterface $to): array
    {
        $labels = [
            'cash' => 'الدفع عند الاستلام',
            'cod' => 'الدفع عند الاستلام',
            'cliq' => 'كليك (CliQ)',
            'card' => 'بطاقة',
            'wallet' => 'محفظة',
        ];

        return $this->revenueQuery($from, $to)
            ->selectRaw('orders.payment_method as payment_method, COUNT(*) as orders_count, COALESCE(SUM(orders.total),0) as total')
            ->groupBy('orders.payment_method')
            ->get()
            ->map(fn ($row) => [
                'method' => $row->payment_method,
                'label' => $labels[$row->payment_method] ?? $row->payment_method,
                'orders' => (int) $row->orders_count,
                'total' => (float) $row->total,
            ])
            ->all();
    }

    /** Coupon redemptions and what they cost. */
    public function couponUsage(CarbonInterface $from, CarbonInterface $to): array
    {
        return DB::table('coupon_usages')
            ->join('coupons', 'coupons.id', '=', 'coupon_usages.coupon_id')
            ->whereBetween('coupon_usages.created_at', [$from->copy()->startOfDay(), $to->copy()->endOfDay()])
            ->selectRaw('coupons.code, coupons.title_ar as title')
            ->selectRaw('COUNT(*) as uses')
            ->selectRaw('COALESCE(SUM(coupon_usages.discount_amount),0) as discount')
            ->groupBy('coupons.code', 'coupons.title_ar')
            ->orderByDesc('uses')
            ->get()
            ->map(fn ($row) => [
                'code' => $row->code,
                'title' => $row->title,
                'uses' => (int) $row->uses,
                'discount' => (float) $row->discount,
            ])
            ->all();
    }

    /** Highest-spending customers. */
    public function topCustomers(CarbonInterface $from, CarbonInterface $to, int $limit = 10): array
    {
        return $this->revenueQuery($from, $to)
            ->join('users', 'users.id', '=', 'orders.user_id')
            ->selectRaw('users.name, users.phone')
            ->selectRaw('COUNT(*) as orders_count')
            ->selectRaw('COALESCE(SUM(orders.total),0) as total')
            ->groupBy('users.id', 'users.name', 'users.phone')
            ->orderByDesc('total')
            ->limit($limit)
            ->get()
            ->map(fn ($row) => [
                'name' => $row->name,
                'phone' => $row->phone,
                'orders' => (int) $row->orders_count,
                'total' => (float) $row->total,
            ])
            ->all();
    }

    /** Expenses grouped by category for the P&L breakdown. */
    public function expensesByCategory(CarbonInterface $from, CarbonInterface $to): array
    {
        return Expense::between($from->copy()->startOfDay(), $to->copy()->endOfDay())
            ->leftJoin('expense_categories', 'expense_categories.id', '=', 'expenses.expense_category_id')
            ->selectRaw('COALESCE(expense_categories.name_ar, "غير مصنّف") as name')
            ->selectRaw('COALESCE(expense_categories.color, "#6B7280") as color')
            ->selectRaw('COALESCE(SUM(expenses.amount),0) as total')
            ->groupBy('name', 'color')
            ->orderByDesc('total')
            ->get()
            ->map(fn ($row) => [
                'name' => $row->name,
                'color' => $row->color,
                'total' => (float) $row->total,
            ])
            ->all();
    }

    /** Named ranges offered by the date filter on every report screen. */
    public static function ranges(): array
    {
        return [
            'today' => 'اليوم',
            'yesterday' => 'أمس',
            'last_7' => 'آخر ٧ أيام',
            'last_30' => 'آخر ٣٠ يوم',
            'this_month' => 'هذا الشهر',
            'last_month' => 'الشهر الماضي',
            'this_year' => 'هذه السنة',
            'custom' => 'فترة مخصّصة',
        ];
    }

    /** Resolve a named range into concrete dates. */
    public static function resolveRange(string $range, ?string $from = null, ?string $to = null): array
    {
        return match ($range) {
            'today' => [Carbon::today(), Carbon::today()],
            'yesterday' => [Carbon::yesterday(), Carbon::yesterday()],
            'last_7' => [Carbon::today()->subDays(6), Carbon::today()],
            'last_30' => [Carbon::today()->subDays(29), Carbon::today()],
            'this_month' => [Carbon::today()->startOfMonth(), Carbon::today()],
            'last_month' => [
                Carbon::today()->subMonthNoOverflow()->startOfMonth(),
                Carbon::today()->subMonthNoOverflow()->endOfMonth(),
            ],
            'this_year' => [Carbon::today()->startOfYear(), Carbon::today()],
            default => [
                $from ? Carbon::parse($from) : Carbon::today()->subDays(29),
                $to ? Carbon::parse($to) : Carbon::today(),
            ],
        };
    }
}
