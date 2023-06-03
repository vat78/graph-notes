package ru.vat78.notes.clients.android.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.NotesFilter

class NotesUiReducer(
    initial: NotesUiState,
    val contextHolder: ApplicationContext,
    val viewModelScope: CoroutineScope
) : Reducer<NotesUiState, NotesUiEvent>(initial) {

    override fun reduce(oldState: NotesUiState, event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.LoadNotes -> {
                setState(NotesUiState(oldState.caption, oldState.notes, ListState.LOADING, oldState.noteTypes))
                viewModelScope.launch {
                    val noteTypes = contextHolder.services.noteTypeStorage.types
                    val notes = if (event.allNotes)
                        contextHolder.services.noteStorage.getNotes(NotesFilter (
                            typesToLoad = noteTypes.values.filter { !it.tag }.map { it.id }
                        ))
                    else
                        emptyList()
                    setState(NotesUiState(oldState.caption, notes, ListState.LOADED, noteTypes))
                }
            }
            is NotesUiEvent.CreateNote -> {
                viewModelScope.launch {
                    val type = if (event.type != null) {
                        event.type
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