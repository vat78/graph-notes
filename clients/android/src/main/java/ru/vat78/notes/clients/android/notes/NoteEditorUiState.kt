package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note

enum class EditFormState {
    NEW,
    CHANGED,
    CLOSED
}

@Immutable
class NoteEditorUiState(
    val note: Note,
    val state: EditFormState = EditFormState.NEW

) : UiState {

}

sealed class NotesEditorUiEvent: UiEvent {
    data class LoadNote(
        val uuid: String
    ): NotesEditorUiEvent()

    data class SaveNote(
        val isNew: Boolean
    ): NotesEditorUiEvent()

    data class CancelChanges(
        val text: String
    ): NotesEditorUiEvent()
}