<?php

namespace App\Livewire\Admin;

use App\Services\ReportingService;
use App\Support\Permissions;
use Livewire\Attributes\Url;

class Accounting extends AdminPage
{
    protected string $permission = Permissions::ACCOUNTING_VIEW;

    #[Url]
    public string $range = 'this_month';

    #[Url]
    public ?string $from = null;

    #[Url]
    public ?string $to = null;

    public function render()
    {
        $reports = app(ReportingService::class);
        [$from, $to] = ReportingService::resolveRange($this->range, $this->from, $this->to);

        $summary = $reports->summary($from, $to);
        $expenseCategories = $reports->expensesByCategory($from, $to);

        return view('livewire.admin.accounting', [
            'ranges' => ReportingService::ranges(),
            'summary' => $summary,
            'expenseCategories' => $expenseCategories,
            'payments' => $reports->paymentBreakdown($from, $to),
            'daily' => $reports->dailyRevenue($from, $to),
            'periodLabel' => $from->translatedFormat('j M Y') . ' — ' . $to->translatedFormat('j M Y'),
        ])->title('المحاسبة والأرباح');
    }
}
