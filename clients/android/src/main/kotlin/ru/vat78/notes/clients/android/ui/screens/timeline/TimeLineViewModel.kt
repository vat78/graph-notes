package ru.vat78.notes.clients.android.ui.screens.timeline

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.data.uploadTextInsertions

class TimeLineViewModel(
    private val appState: AppState,
) : BaseViewModel<TimeLineState, TimeLineEvent>(
    initialState = TimeLineState(
        caption = "",
        notes = emptyList(),
        state = ListState.INIT
    )
) {

    private val services
        get() = appState.context.services
    private val noteTypes
        get() = services.noteTypeStorage.types

    override fun sendEvent(event: TimeLineEvent) {
        when (event) {
            is TimeLineEvent.LoadNotes -> loadNotes(state.value)
            is TimeLineEvent.CreateNote -> createNote(event.text)
        }
    }

    private fun loadNotes(oldState: TimeLineState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(
            TimeLineState(
                caption = oldState.caption,
                notes = emptyList(),
                state = ListState.LOADING
            )
        )
        viewModelScope.launch {
            val notes = services.noteStorage.getNotes(NotesFilter (
                typesToLoad = noteTypes.values.filter { !it.tag }.map { it.id }
            ))
            _state.emit(
                TimeLineState(
                    caption = oldState.caption,
                    notes = notes,
                    state = ListState.LOADED
                )
            )
            val filledNotes = uploadTextInsertions(notes, {services.noteStorage.getTags(it) }, { services.noteStorage.updateNote(it) })
            _state.emit(
                TimeLineState(
                    caption = oldState.caption,
                    notes = filledNotes,
                    state = ListState.LOADED
                )
            )
        }
    }

    private fun createNote(text: String) {
        val type = noteTypes.values.first { it.default }
        services.noteStorage.buildNewNote(type, text)
    }
}