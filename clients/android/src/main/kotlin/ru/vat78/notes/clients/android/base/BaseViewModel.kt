package ru.vat78.notes.clients.android.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface UiState {
}

interface UiEvent

abstract class BaseViewModel<S : UiState, E : UiEvent>(initialState: S): ViewModel() {

    protected val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S>
        get() = _state

    abstract fun sendEvent(event: E)

}

enum class ListState {
    INIT, LOADING, LOADED
}