package ru.vat78.notes.clients.android.ui.screens.timeline

import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.base.UiEvent
import ru.vat78.notes.clients.android.base.UiState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType

data class TimeLineState(
    val caption: String,
    val notes: List<Note>,
    val state: ListState
) : UiState

sealed class TimeLineEvent() : UiEvent {
    data class CreateNote(
        val text: String,
        val type: NoteType? = null
    ): TimeLineEvent()

    data class LoadNotes(
        val allNotes: Boolean
    ): TimeLineEvent()
}