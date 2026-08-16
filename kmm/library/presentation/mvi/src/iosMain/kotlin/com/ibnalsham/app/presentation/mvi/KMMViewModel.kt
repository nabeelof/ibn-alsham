package com.ibnalsham.app.presentation.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

actual open class KMMViewModel {
    actual val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    protected actual open fun onCleared() {
        viewModelScope.cancel()
    }
}
