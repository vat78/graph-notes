package ru.vat78.notes.clients.android.ui.screens.editor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
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
    CLOSED,
    ERROR
}

enum class DescriptionFocusState {
    HIDE,
    SHOW,
    FOCUSED
}

@Immutable
data class NoteEditorUiState(
    val origin: NoteWithParents,
    val changed: NoteWithParents,
    val noteType: NoteType,
    val availableTypes: Collection<NoteType>,
    val status: EditFormState,
    val descriptionFocus: DescriptionFocusState,
    val descriptionTextValue: TextFieldValue,
    val suggestions: List<DictionaryElement> = emptyList(),
    val newTag: DictionaryElement? = null,
    val errorMessage: Int? = null
) : UiState

sealed class NotesEditorUiEvent: UiEvent {
    data class ResetState(
        val uuid: String
    ): NotesEditorUiEvent()

    data class LoadNote(
        val uuid: String
    ): NotesEditorUiEvent()

    object SaveNote: NotesEditorUiEvent()

    object CancelError: NotesEditorUiEvent()

    object CancelChanges: NotesEditorUiEvent()

    sealed class ChangeEvent : NotesEditorUiEvent() {
        data class ChangeCaption(
            val text: String
        ) : ChangeEvent()

        data class ChangeDescription(
            val text: TextFieldValue
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

    data class AddChildTags(
        val mainTags: Iterable<DictionaryElement>,
    ): NotesEditorUiEvent()

    data class RemoveTag(
        val tag: DictionaryElement
    ): NotesEditorUiEvent()

    data class RequestSuggestions(
        val text: String
    ): NotesEditorUiEvent()

    data class ChangeDescriptionFocus(
        val focus: DescriptionFocusState
    ): NotesEditorUiEvent()

    data class CreateNewTag(
        val tag: DictionaryElement
    ): NotesEditorUiEvent()

    object CancelNewTag: NotesEditorUiEvent()

    data class ChangeNewTagType(
        val tag: DictionaryElement,
        val type: NoteType
    ): NotesEditorUiEvent()
}