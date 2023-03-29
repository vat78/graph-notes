package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note

@Immutable
class NotesUiState(
    val caption: String,
    val notes: List<Note>
) : UiState {
    companion object {
        fun initial() = NotesUiState("", listOf())
    }
}

sealed class NotesUiEvent: UiEvent {
    data class CreateNote(
        val text: String
    ): NotesUiEvent()
}