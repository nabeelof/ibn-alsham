<div>
    <p class="text-sm text-slate-500 mb-6">
        هذه القيم تظهر داخل التطبيق. كل حقل له نسخة عربية وأخرى إنجليزية.
    </p>

    <form wire:submit="save" class="space-y-6">
        @foreach($groups as $groupKey => $group)
            <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
                <div class="px-5 py-3.5 border-b border-slate-100 bg-slate-50">
                    <h3 class="font-bold text-slate-800">{{ $group['label'] }}</h3>
                </div>
                <div class="p-5 space-y-5">
                    @foreach($group['keys'] as $key => $meta)
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-2">{{ $meta['label'] }}</label>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                                <div>
                                    <span class="block text-[10px] font-bold text-slate-400 mb-1">عربي</span>
                                    @if($meta['type'] === 'textarea')
                                        <textarea wire:model="values.{{ $key }}.ar" rows="3"
                                                  class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500"></textarea>
                                    @else
                                        <input type="text" wire:model="values.{{ $key }}.ar"
                                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                                    @endif
                                </div>
                                <div>
                                    <span class="block text-[10px] font-bold text-slate-400 mb-1">English</span>
                                    @if($meta['type'] === 'textarea')
                                        <textarea wire:model="values.{{ $key }}.en" rows="3" dir="ltr"
                                                  class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500"></textarea>
                                    @else
                                        <input type="text" wire:model="values.{{ $key }}.en" dir="ltr"
                                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                                    @endif
                                </div>
                            </div>
                        </div>
                    @endforeach
                </div>
            </div>
        @endforeach

        <div class="sticky bottom-4">
            <button type="submit"
                    class="w-full px-4 py-3 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 shadow-lg">
                حفظ جميع الإعدادات
            </button>
        </div>
    </form>
</div>
