package com.ibnalsham.app.presentation.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Swift-friendly observation helpers.
 *
 * Kotlin `Flow` is not directly consumable from Swift, so SwiftUI bridges call
 * these and store the returned [DisposableHandle] until the view goes away.
 */
fun <T> StateFlow<T>.watch(scope: CoroutineScope, onNext: (T) -> Unit): DisposableHandle {
    val job: Job = scope.launch {
        collect { onNext(it) }
    }
    return DisposableHandle { job.cancel() }
}

fun <T> Flow<T>.watchFlow(scope: CoroutineScope, onNext: (T) -> Unit): DisposableHandle {
    val job: Job = scope.launch {
        collect { onNext(it) }
    }
    return DisposableHandle { job.cancel() }
}

/** Observe the ViewModel's state from SwiftUI. */
fun <State : UiState, Action : UiEvent, Effect : UiSideEffect> MVI<State, Action, Effect>.watchState(
    scope: CoroutineScope,
    onNext: (State) -> Unit,
): DisposableHandle = uiState.watch(scope, onNext)

/** Observe one-shot side effects (navigation, snackbars) from SwiftUI. */
fun <State : UiState, Action : UiEvent, Effect : UiSideEffect> MVI<State, Action, Effect>.watchEffect(
    scope: CoroutineScope,
    onNext: (Effect) -> Unit,
): DisposableHandle = sideEffects.watchFlow(scope, onNext)
