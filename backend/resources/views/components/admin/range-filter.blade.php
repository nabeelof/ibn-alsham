@props(['ranges', 'range', 'showCustom' => false])

<div class="bg-white rounded-xl border border-slate-200 p-4 mb-6 flex flex-wrap items-end gap-3">
    <div>
        <label class="block text-[11px] font-bold text-slate-500 mb-1">الفترة</label>
        <select wire:model.live="range"
                class="px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500">
            @foreach($ranges as $key => $label)
                <option value="{{ $key }}">{{ $label }}</option>
            @endforeach
        </select>
    </div>

    @if($showCustom)
        <div>
            <label class="block text-[11px] font-bold text-slate-500 mb-1">من</label>
            <input type="date" wire:model.live="from"
                   class="px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
        </div>
        <div>
            <label class="block text-[11px] font-bold text-slate-500 mb-1">إلى</label>
            <input type="date" wire:model.live="to"
                   class="px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
        </div>
    @endif

    {{ $slot }}
</div>
