package com.ibnalsham.app.presentation.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MviDelegate<State : UiState, Action : UiEvent, Effect : UiSideEffect>(
    initialState: State
) : MVI<State, Action, Effect> {

    private val _uiState = MutableStateFlow(initialState)
    override val uiState = _uiState.asStateFlow()
    override val currentState: State get() = uiState.value

    private val _sideEffects = MutableSharedFlow<Effect>()
    override val sideEffects = _sideEffects.asSharedFlow()

    override fun onAction(event: Action) {
        // To be overridden by ViewModels
    }

    override fun updateState(block: State.() -> State) {
        _uiState.update(block)
    }

    override suspend fun emitEffect(effect: () -> Effect) {
        _sideEffects.emit(effect())
    }
}

fun <State : UiState, Action : UiEvent, Effect : UiSideEffect> mvi(
    initialState: State
): MVI<State, Action, Effect> = MviDelegate(initialState)
