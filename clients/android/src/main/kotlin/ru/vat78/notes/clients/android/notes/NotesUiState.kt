package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType

@Immutable
class NotesUiState(
    val caption: String,
    val notes: List<Note>,
    val noteTypes: List<NoteType> = emptyList(),
) : UiState {

}

sealed class NotesUiEvent: UiEvent {
    data class CreateNote(
        val text: String,
        val type: NoteType? = null
    ): NotesUiEvent()

    data class LoadNotes(
        val types: List<String>
    ): NotesUiEvent()
}