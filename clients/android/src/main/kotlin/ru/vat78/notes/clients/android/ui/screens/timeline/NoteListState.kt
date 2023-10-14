package ru.vat78.notes.clients.android.ui.screens.timeline

import androidx.compose.ui.text.input.TextFieldValue
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.SortingType
import java.util.SortedSet

data class NoteListState(
    val rootNote: DictionaryElement?,
    val caption: String,
    val notes: SortedSet<Note>,
    val state: ListState,
    val inputValue: TextFieldValue,
    val selectedSuggestions: Set<DictionaryElement> = emptySet(),
    val suggestions: List<DictionaryElement> = emptyList(),
    val newTag: DictionaryElement? = null,
    val sortingType: SortingType = SortingType.FINISH_TIME_DESC,
    val error: Int? = null,
) : UiState

sealed class NoteListEvent() : UiEvent {
    data class CreateNote(
        val text: String,
        val type: NoteType? = null
    ): NoteListEvent()

    object LoadNotes: NoteListEvent()

    data class LoadNotesByParent(
        val parentId: String
    ): NoteListEvent()

    data class NewTextInput(
        val textInput: TextFieldValue
    ): NoteListEvent()

    data class SelectSuggestion(
        val tag: DictionaryElement
    ): NoteListEvent()

    data class CreateNewTag(
        val tag: DictionaryElement
    ): NoteListEvent()

    object CancelNewTag: NoteListEvent()

    data class ChangeNewTagType(
        val tag: DictionaryElement,
        val type: NoteType
    ): NoteListEvent()
}