package ru.vat78.notes.clients.android.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.NotesStorage

class NoteEditorUiReducer(
    initial: NoteEditorUiState,
    val noteStorage: NotesStorage,
    val viewModelScope: CoroutineScope
) : Reducer<NoteEditorUiState, NotesEditorUiEvent>(initial) {

    override fun reduce(oldState: NoteEditorUiState, event: NotesEditorUiEvent) {
        when (event) {
            is NotesEditorUiEvent.LoadNote -> {
                viewModelScope.launch {
                    val note = noteStorage.getOneNote(event.uuid)
                    val state = if (event.uuid == "new") EditFormState.CHANGED else EditFormState.NEW
                    setState(
                        NoteEditorUiState(
                            note = note,
                            state = state
                        )
                    )
                }
            }
            is NotesEditorUiEvent.SaveNote -> {
                viewModelScope.launch {
                    if (oldState.state == EditFormState.CHANGED) {
                        noteStorage.saveNote(oldState.note)
                    }
                    setState(
                        NoteEditorUiState(
                            note = oldState.note,
                            state = EditFormState.CLOSED
                        )
                    )
                }
            }
            is NotesEditorUiEvent.CancelChanges -> {
                viewModelScope.launch {
                    setState(
                        NoteEditorUiState(
                            note = oldState.note,
                            state = EditFormState.CLOSED
                        )
                    )
                }
            }
        }
    }
}