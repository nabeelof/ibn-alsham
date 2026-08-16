<div>
    <div class="bg-white rounded-xl border border-slate-200 p-4 mb-6 flex flex-wrap items-end gap-3">
        <div class="flex-1 min-w-[200px]">
            <label class="block text-[11px] font-bold text-slate-500 mb-1">بحث</label>
            <input type="text" wire:model.live.debounce.400ms="search" placeholder="الاسم أو البريد…"
                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
        </div>
        <button wire:click="openForm"
                class="px-4 py-2 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800 transition">
            + مشرف جديد
        </button>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table class="w-full text-right">
            <thead class="bg-slate-50 text-slate-500 text-xs">
                <tr>
                    <th class="px-5 py-3 font-bold">الاسم</th>
                    <th class="px-5 py-3 font-bold">البريد الإلكتروني</th>
                    <th class="px-5 py-3 font-bold">الدور</th>
                    <th class="px-5 py-3 font-bold">الحالة</th>
                    <th class="px-5 py-3 font-bold">آخر دخول</th>
                    <th class="px-5 py-3 font-bold">إجراءات</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
                @forelse($admins as $admin)
                    <tr class="hover:bg-slate-50">
                        <td class="px-5 py-3">
                            <p class="text-sm font-bold text-slate-800">{{ $admin->name }}</p>
                            @if($admin->id === $currentAdminId)
                                <span class="text-[10px] font-bold text-emerald-600">أنت</span>
                            @endif
                        </td>
                        <td class="px-5 py-3 text-sm text-slate-600" dir="ltr">{{ $admin->email }}</td>
                        <td class="px-5 py-3">
                            <span class="px-2.5 py-1 text-[11px] font-bold text-slate-700 bg-slate-100 rounded-full">
                                {{ $admin->roleLabel() }}
                            </span>
                        </td>
                        <td class="px-5 py-3">
                            <span class="px-2.5 py-1 text-[11px] font-bold rounded-full
                                {{ $admin->is_active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600' }}">
                                {{ $admin->is_active ? 'نشط' : 'معطّل' }}
                            </span>
                        </td>
                        <td class="px-5 py-3 text-xs text-slate-500">
                            {{ $admin->last_login_at?->diffForHumans() ?? '—' }}
                        </td>
                        <td class="px-5 py-3 whitespace-nowrap">
                            <button wire:click="openForm({{ $admin->id }})"
                                    class="text-xs font-bold text-blue-600 hover:underline">تعديل</button>
                            @if($admin->id !== $currentAdminId)
                                <button wire:click="toggleActive({{ $admin->id }})"
                                        class="mr-3 text-xs font-bold text-amber-600 hover:underline">
                                    {{ $admin->is_active ? 'تعطيل' : 'تفعيل' }}
                                </button>
                                <button wire:click="delete({{ $admin->id }})"
                                        wire:confirm="هل تريد حذف هذا المشرف؟"
                                        class="mr-3 text-xs font-bold text-red-600 hover:underline">حذف</button>
                            @endif
                        </td>
                    </tr>
                @empty
                    <tr><td colspan="6" class="px-5 py-14 text-center text-sm text-slate-400">لا يوجد مشرفون</td></tr>
                @endforelse
            </tbody>
        </table>
        <div class="px-5 py-3 border-t border-slate-100">{{ $admins->links() }}</div>
    </div>

    @if($showForm)
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div class="bg-white rounded-xl w-full max-w-lg">
                <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                    <h3 class="font-bold text-slate-800">{{ $editingId ? 'تعديل مشرف' : 'مشرف جديد' }}</h3>
                    <button wire:click="$set('showForm', false)" class="text-slate-400 hover:text-slate-700 text-xl">&times;</button>
                </div>
                <form wire:submit="save" class="p-6 space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الاسم</label>
                        <input type="text" wire:model="name"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                        @error('name') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">البريد الإلكتروني</label>
                            <input type="email" wire:model="email" dir="ltr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            @error('email') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-slate-600 mb-1">رقم الهاتف</label>
                            <input type="text" wire:model="phone" dir="ltr"
                                   class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg">
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">الدور</label>
                        <select wire:model="roleId"
                                class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                            <option value="">اختر الدور…</option>
                            @foreach($roles as $role)
                                <option value="{{ $role->id }}">{{ $role->name_ar }}</option>
                            @endforeach
                        </select>
                        @error('roleId') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-slate-600 mb-1">
                            كلمة المرور {{ $editingId ? '(اتركها فارغة للإبقاء على الحالية)' : '' }}
                        </label>
                        <input type="password" wire:model="password" dir="ltr" autocomplete="new-password"
                               class="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-red-500">
                        @error('password') <span class="text-xs text-red-600">{{ $message }}</span> @enderror
                    </div>

                    <label class="flex items-center gap-2 text-sm text-slate-700">
                        <input type="checkbox" wire:model="isActive" class="rounded text-red-700">
                        الحساب نشط
                    </label>

                    <div class="flex gap-3 pt-2">
                        <button type="submit"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-white bg-red-700 rounded-lg hover:bg-red-800">حفظ</button>
                        <button type="button" wire:click="$set('showForm', false)"
                                class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-700 bg-slate-100 rounded-lg hover:bg-slate-200">إلغاء</button>
                    </div>
                </form>
            </div>
        </div>
    @endif
</div>
