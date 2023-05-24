package ru.vat78.notes.clients.android.notes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteWithLinks
import ru.vat78.notes.clients.android.data.defaultTypes

class NoteEditorViewModel(
    contextHolder: ApplicationContext,
) : BaseViewModel<NoteEditorUiState, NotesEditorUiEvent>() {

    private val reducer = NoteEditorUiReducer(
        initial = NoteEditorUiState(
            note = NoteWithLinks(Note(""), emptySet()),
            noteType = defaultTypes[0],
            status = EditFormState.NEW,
        ),
        contextHolder = contextHolder,
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<NoteEditorUiState>
        get() = reducer.state

    fun sendEvent(event: NotesEditorUiEvent) {
        reducer.sendEvent(event)
    }
}