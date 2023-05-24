package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.NoteWithLinks
import ru.vat78.notes.clients.android.data.ObjectType
import java.time.LocalDateTime

enum class EditFormState {
    NEW,
    LOADED,
    CHANGED,
    CLOSED
}

@Immutable
data class NoteEditorUiState(
    val note: NoteWithLinks,
    val noteType: ObjectType,
    val status: EditFormState = EditFormState.NEW,
    val suggestions: List<DictionaryElement> = emptyList()
) : UiState

sealed class NotesEditorUiEvent: UiEvent {
    data class ResetState(
        val uuid: String
    ): NotesEditorUiEvent()

    data class LoadNote(
        val uuid: String
    ): NotesEditorUiEvent()

    data class SaveNote(
        val isNew: Boolean
    ): NotesEditorUiEvent()

    data class CancelChanges(
        val text: String
    ): NotesEditorUiEvent()

    data class ChangeCaption(
        val text: String
    ): NotesEditorUiEvent()

    data class ChangeDescription(
        val text: String
    ): NotesEditorUiEvent()

    data class ChangeType(
        val type: ObjectType
    ): NotesEditorUiEvent()

    data class ChangeStart(
        val startTime: LocalDateTime
    ): NotesEditorUiEvent()

    data class ChangeFinish(
        val finishTime: LocalDateTime
    ): NotesEditorUiEvent()

    data class AlignStartTime(
        val currentValue: LocalDateTime
    ): NotesEditorUiEvent()

    data class AddTag(
        val newTag: DictionaryElement,
    ): NotesEditorUiEvent()

    data class RemoveTag(
        val tag: DictionaryElement
    ): NotesEditorUiEvent()

    data class RequestSuggestions(
        val text: String
    ): NotesEditorUiEvent()
}