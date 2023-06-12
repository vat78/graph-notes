package ru.vat78.notes.clients.android.ui.screens.editor

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteWithParents
import java.time.ZonedDateTime

enum class EditFormState {
    NEW,
    LOADED,
    CHANGED,
    CLOSED
}

@Immutable
data class NoteEditorUiState(
    val origin: NoteWithParents,
    val changed: NoteWithParents,
    val noteType: NoteType,
    val availableTypes: Collection<NoteType>,
    val status: EditFormState,
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

    object CancelChanges: NotesEditorUiEvent()

    sealed class ChangeEvent : NotesEditorUiEvent() {
        data class ChangeCaption(
            val text: String
        ) : ChangeEvent()

        data class ChangeDescription(
            val text: String
        ) : ChangeEvent()

        data class ChangeType(
            val type: NoteType
        ) : ChangeEvent()

        data class ChangeStart(
            val startTime: ZonedDateTime
        ) : ChangeEvent()

        data class ChangeFinish(
            val finishTime: ZonedDateTime
        ) : ChangeEvent()
    }

    data class AlignStartTime(
        val currentValue: ZonedDateTime
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