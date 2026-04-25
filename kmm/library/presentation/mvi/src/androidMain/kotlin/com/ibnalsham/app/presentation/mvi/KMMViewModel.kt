package com.ibnalsham.app.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope as androidViewModelScope
import kotlinx.coroutines.CoroutineScope

actual open class KMMViewModel : ViewModel() {
    actual val viewModelScope: CoroutineScope = androidViewModelScope
    
    protected actual override fun onCleared() {
        super.onCleared()
    }
}
