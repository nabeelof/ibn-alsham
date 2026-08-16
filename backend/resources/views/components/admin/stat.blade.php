@props([
    'label',
    'value',
    'sub' => null,
    'tone' => 'slate',
    'icon' => null,
])

@php
    $tones = [
        'slate' => 'text-slate-900',
        'red' => 'text-red-600',
        'green' => 'text-emerald-600',
        'blue' => 'text-blue-600',
        'amber' => 'text-amber-600',
    ];
    $valueClass = $tones[$tone] ?? $tones['slate'];
@endphp

<div class="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
    <div class="flex items-start justify-between">
        <p class="text-xs font-bold text-slate-500">{{ $label }}</p>
        @if($icon)
            <x-admin.icon :name="$icon" class="w-4 h-4 text-slate-300" />
        @endif
    </div>
    <p class="mt-2 text-2xl font-extrabold {{ $valueClass }}">{{ $value }}</p>
    @if($sub)
        <p class="mt-1 text-[11px] font-semibold text-slate-400">{{ $sub }}</p>
    @endif
</div>
