package ru.vat78.notes.clients.android.notes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NotesStorage

class NoteEditorViewModel(
    storage: NotesStorage
) : BaseViewModel<NoteEditorUiState, NotesEditorUiEvent>() {

    private val reducer = NoteEditorUiReducer(
        initial = NoteEditorUiState(
            note = Note(
                type = NoteType.NOTE.toString(),
                caption = "",
                description = ""
            )
        ),
        noteStorage = storage,
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<NoteEditorUiState>
        get() = reducer.state

    fun sendEvent(event: NotesEditorUiEvent) {
        reducer.sendEvent(event)
    }
}