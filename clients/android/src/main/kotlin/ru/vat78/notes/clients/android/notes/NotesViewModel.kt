package ru.vat78.notes.clients.android.notes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.BaseViewModel

class NotesViewModel(
    contextHolder: ApplicationContext,
) : BaseViewModel<NotesUiState, NotesUiEvent>() {

    private val reducer = NotesUiReducer(
        initial = NotesUiState(
            caption = "Timeline",
            notes = emptyList()
        ),
        contextHolder = contextHolder,
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<NotesUiState>
        get() = reducer.state

    fun sendEvent(event: NotesUiEvent) {
        reducer.sendEvent(event)
    }
}