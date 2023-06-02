package ru.vat78.notes.clients.android.tags

import androidx.compose.runtime.Immutable
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.NoteType

@Immutable
class TagsUiState(
    tagType: NoteType? = null

) : UiState {
}

sealed class TagsUiEvent: UiEvent {
    data class LoadData(
        val type: String,
        val tag: String?
    ): TagsUiEvent()

}