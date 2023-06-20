package ru.vat78.notes.clients.android.ui.screens.timeline

import androidx.compose.ui.text.input.TextFieldValue
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType

data class TimeLineState(
    val caption: String,
    val notes: List<Note>,
    val state: ListState,
    val inputValue: TextFieldValue,
    val selectedSuggestions: Set<DictionaryElement> = emptySet(),
    val suggestions: List<DictionaryElement> = emptyList()
) : UiState

sealed class TimeLineEvent() : UiEvent {
    data class CreateNote(
        val text: String,
        val type: NoteType? = null
    ): TimeLineEvent()

    data class LoadNotes(
        val allNotes: Boolean
    ): TimeLineEvent()

    data class NewTextInput(
        val textInput: TextFieldValue
    ): TimeLineEvent()

    data class SelectSuggestion(
        val tag: DictionaryElement
    ): TimeLineEvent()
}