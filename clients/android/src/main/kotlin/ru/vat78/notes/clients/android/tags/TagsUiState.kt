package ru.vat78.notes.clients.android.tags

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.notes.ListState

@Immutable
class TagsUiState(
    val tagType: NoteType? = null,
    val caption: String = "",
    val rootNote: Note? = null,
    val tags: List<Note> = emptyList(),
    val noteTypes: Map<String, NoteType> = emptyMap(),
    val state: ListState = ListState.LOADED
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