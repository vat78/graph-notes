package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType

@Immutable
class NotesUiState(
    val caption: String,
    val notes: List<Note>
) : UiState {

}

sealed class NotesUiEvent: UiEvent {
    data class CreateNote(
        val type: NoteType,
        val text: String
    ): NotesUiEvent()

    data class LoadNotes(
        val type: NoteType?
    ): NotesUiEvent()
}