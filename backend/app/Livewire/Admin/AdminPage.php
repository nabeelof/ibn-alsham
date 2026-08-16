<?php

namespace App\Livewire\Admin;

use Livewire\Attributes\Layout;
use Livewire\Component;

/**
 * Base for every admin screen: applies the shared layout and refuses to mount
 * for an admin who lacks the screen's permission.
 */
#[Layout('components.layouts.admin')]
abstract class AdminPage extends Component
{
    /** Permission required to open this screen. */
    protected string $permission = '';

    public function mount(): void
    {
        $this->authorizePage();
        $this->initPage();
    }

    /**
     * Screens override this instead of mount() so the guard always runs.
     * Named initPage (not booted) because Livewire reserves the boot* hooks.
     */
    protected function initPage(): void
    {
        //
    }

    protected function authorizePage(): void
    {
        $admin = auth('admin')->user();

        abort_unless(
            $admin && ($this->permission === '' || $admin->hasPermission($this->permission)),
            403,
            'ليس لديك صلاحية للوصول إلى هذه الصفحة'
        );
    }

    /** Guard a single action inside a screen (e.g. delete vs view). */
    protected function requirePermission(string $permission): void
    {
        abort_unless(
            auth('admin')->user()?->hasPermission($permission),
            403,
            'ليس لديك صلاحية لتنفيذ هذا الإجراء'
        );
    }

    protected function can(string $permission): bool
    {
        return (bool) auth('admin')->user()?->hasPermission($permission);
    }
}
