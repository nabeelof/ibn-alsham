package com.ibnalsham.app.presentation.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope

open class UiEvent

interface UiState

interface UiSideEffect

interface MVI<State : UiState, Action : UiEvent, Effect : UiSideEffect> {
    val uiState: StateFlow<State>
    val currentState: State
    val sideEffects: Flow<Effect>

    fun onAction(event: Action)
    fun updateState(block: State.() -> State)
    suspend fun emitEffect(effect: () -> Effect)
    
    // iOS observation helpers
    // fun watchState(scope: CoroutineScope, onNext: (State) -> Unit): DisposableHandle
    // fun watchEffect(scope: CoroutineScope, onNext: (Effect) -> Unit): DisposableHandle
}

expect open class KMMViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
