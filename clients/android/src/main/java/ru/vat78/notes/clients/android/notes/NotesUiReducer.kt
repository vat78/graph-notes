package ru.vat78.notes.clients.android.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.Note

class NotesUiReducer(
    initial: NotesUiState,
    val viewModelScope: CoroutineScope
) : Reducer<NotesUiState, NotesUiEvent>(initial) {

    override fun reduce(oldState: NotesUiState, event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.CreateNote -> {
                viewModelScope.launch {
                    val caption = event.text.lines().firstOrNull() ?: ""
                    val description = event.text.subSequence(caption.length, event.text.length).trim().toString()
                    val newNote = Note(
                        caption = caption,
                        description = description
                    )
                    setState(NotesUiState(oldState.caption, listOf(newNote) + oldState.notes))
                }
            }
        }
    }
}