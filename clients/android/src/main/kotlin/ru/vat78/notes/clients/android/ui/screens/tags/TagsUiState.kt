package ru.vat78.notes.clients.android.ui.screens.tags

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType

@Immutable
data class TagsUiState(
    val tagType: NoteType,
    val caption: String,
    val rootNote: Note? = null,
    val tags: List<Note> = emptyList(),
    val state: ListState = ListState.INIT
    ) : UiState {
}

sealed class TagsUiEvent: UiEvent {
    data class LoadData(
        val type: String,
        val tag: String?
    ): TagsUiEvent()

    data class CreateTag(
        val type: NoteType,
        val caption: String,
        val parent: Note?
    ): TagsUiEvent()
}