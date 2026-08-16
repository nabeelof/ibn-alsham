<?php

namespace App\Livewire\Admin;

use App\Services\ReportingService;
use App\Support\Permissions;
use Livewire\Attributes\Url;
use Symfony\Component\HttpFoundation\StreamedResponse;

class Reports extends AdminPage
{
    protected string $permission = Permissions::REPORTS_VIEW;

    #[Url]
    public string $range = 'last_30';

    #[Url]
    public ?string $from = null;

    #[Url]
    public ?string $to = null;

    #[Url]
    public string $tab = 'sales';

    public function selectTab(string $tab): void
    {
        $this->tab = $tab;
    }

    /** Download the active tab as CSV. */
    public function export(): ?StreamedResponse
    {
        $this->requirePermission(Permissions::REPORTS_EXPORT);

        [$rows, $headers, $filename] = $this->exportPayload();

        return response()->streamDownload(function () use ($rows, $headers) {
            $handle = fopen('php://output', 'w');
            // BOM so Excel opens Arabic correctly.
            fwrite($handle, "\xEF\xBB\xBF");
            fputcsv($handle, $headers);
            foreach ($rows as $row) {
                fputcsv($handle, $row);
            }
            fclose($handle);
        }, $filename, ['Content-Type' => 'text/csv; charset=UTF-8']);
    }

    private function exportPayload(): array
    {
        $reports = app(ReportingService::class);
        [$from, $to] = $this->resolved();
        $stamp = $from->toDateString() . '_' . $to->toDateString();

        return match ($this->tab) {
            'products' => [
                array_map(fn ($r) => [$r['name'], $r['qty'], $r['revenue'], $r['profit']], $reports->topProducts($from, $to, 200)),
                ['المنتج', 'الكمية', 'المبيعات', 'الربح'],
                "products_{$stamp}.csv",
            ],
            'categories' => [
                array_map(fn ($r) => [$r['name'], $r['qty'], $r['revenue']], $reports->salesByCategory($from, $to)),
                ['القسم', 'الكمية', 'المبيعات'],
                "categories_{$stamp}.csv",
            ],
            'customers' => [
                array_map(fn ($r) => [$r['name'], $r['phone'], $r['orders'], $r['total']], $reports->topCustomers($from, $to, 500)),
                ['الزبون', 'الهاتف', 'عدد الطلبات', 'الإجمالي'],
                "customers_{$stamp}.csv",
            ],
            'coupons' => [
                array_map(fn ($r) => [$r['code'], $r['title'], $r['uses'], $r['discount']], $reports->couponUsage($from, $to)),
                ['الكود', 'الوصف', 'مرات الاستخدام', 'إجمالي الخصم'],
                "coupons_{$stamp}.csv",
            ],
            default => [
                array_map(fn ($r) => [$r['day'], $r['orders'], $r['total']], $reports->dailyRevenue($from, $to)),
                ['التاريخ', 'عدد الطلبات', 'المبيعات'],
                "sales_{$stamp}.csv",
            ],
        };
    }

    private function resolved(): array
    {
        return ReportingService::resolveRange($this->range, $this->from, $this->to);
    }

    public function render()
    {
        $reports = app(ReportingService::class);
        [$from, $to] = $this->resolved();

        return view('livewire.admin.reports', [
            'ranges' => ReportingService::ranges(),
            'summary' => $reports->summary($from, $to),
            'daily' => $reports->dailyRevenue($from, $to),
            'topProducts' => $reports->topProducts($from, $to, 25),
            'categories' => $reports->salesByCategory($from, $to),
            'customers' => $reports->topCustomers($from, $to, 25),
            'coupons' => $reports->couponUsage($from, $to),
            'typeSplit' => $reports->orderTypeSplit($from, $to),
            'canExport' => $this->can(Permissions::REPORTS_EXPORT),
            'periodLabel' => $from->translatedFormat('j M Y') . ' — ' . $to->translatedFormat('j M Y'),
        ])->title('التقارير');
    }
}
