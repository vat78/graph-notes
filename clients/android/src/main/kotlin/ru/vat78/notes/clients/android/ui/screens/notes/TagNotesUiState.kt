package ru.vat78.notes.clients.android.ui.screens.notes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note

@Immutable
data class TagNotesUiState(
    val rootNote: Note,
    val notes: List<Note>,
    val state: ListState,
    val inputValue: TextFieldValue,
    val selectedSuggestions: Set<DictionaryElement> = emptySet(),
    val suggestions: List<DictionaryElement> = emptyList()
    ) : UiState {
}

sealed class TagNotesUiEvent: UiEvent {
    data class LoadData(
        val tagId: String
    ): TagNotesUiEvent()

    data class CreateTag(
        val caption: String,
        val parent: Note
    ): TagNotesUiEvent()

    data class NewTextInput(
        val textInput: TextFieldValue
    ): TagNotesUiEvent()

    data class SelectSuggestion(
        val tag: DictionaryElement
    ): TagNotesUiEvent()
}