package ru.vat78.notes.clients.android.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.Reducer

class NotesUiReducer(
    initial: NotesUiState,
    val contextHolder: ApplicationContext,
    val viewModelScope: CoroutineScope
) : Reducer<NotesUiState, NotesUiEvent>(initial) {

    override fun reduce(oldState: NotesUiState, event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.LoadNotes -> {
                viewModelScope.launch {
                    val notes = contextHolder.services.noteStorage.getNotes(event.types)
                    setState(NotesUiState(oldState.caption, notes))
                }
            }
            is NotesUiEvent.CreateNote -> {
                viewModelScope.launch {
                    val type = if (event.type != null) {
                        event.type
                    } else if (oldState.noteTypes.isNotEmpty()) {
                        oldState.noteTypes[0]
                    } else {
                        contextHolder.services.noteTypeStorage.getDefaultType()
                    }
                    contextHolder.services.noteStorage.buildNewNote(
                        type,
                        event.text
                    )
                }
            }
        }
    }
}