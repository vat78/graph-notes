package ru.vat78.notes.clients.android.ui.screens.notes

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note

@Immutable
data class TagNotesUiState(
    val rootNote: Note,
    val notes: List<Note> = emptyList(),
    val state: ListState = ListState.INIT
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
}