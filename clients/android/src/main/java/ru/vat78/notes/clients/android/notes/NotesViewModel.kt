package ru.vat78.notes.clients.android.notes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.NotesStorage

class NotesViewModel(
    storage: NotesStorage
) : BaseViewModel<NotesUiState, NotesUiEvent>() {

    private val reducer = NotesUiReducer(
        initial = NotesUiState(
            caption = "Test",
            notes = emptyList()
        ),
        notesStorage = storage,
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<NotesUiState>
        get() = reducer.state

    fun sendEvent(event: NotesUiEvent) {
        reducer.sendEvent(event)
    }
}