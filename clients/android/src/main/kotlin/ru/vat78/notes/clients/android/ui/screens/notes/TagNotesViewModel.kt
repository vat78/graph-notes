package ru.vat78.notes.clients.android.ui.screens.notes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteWithChildren
import ru.vat78.notes.clients.android.data.NotesFilter

class TagNotesViewModel(
    private val appState: AppState,
) : BaseViewModel<TagNotesUiState, TagNotesUiEvent>(
    initialState = TagNotesUiState(
        rootNote = Note(),
        state = ListState.INIT
    )
) {

    private val services
        get() = appState.context.services

    private val noteTypes
        get() = services.noteTypeStorage.types

    override fun sendEvent(event: TagNotesUiEvent) {
        when (event) {
            is TagNotesUiEvent.LoadData -> loadData(event.tagId, state.value)
            is TagNotesUiEvent.CreateTag -> createNote(event.caption, event.parent)
        }
    }

    private fun loadData(tagId: String, oldState: TagNotesUiState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(state.value.copy(state = ListState.LOADING))

        viewModelScope.launch {
            val note = services.noteStorage.getNoteWithChildren(tagId)
            val values = getChildNotesWithHierarchy(note).sortedBy { it.finish }
            _state.emit(
                TagNotesUiState(
                    notes = values,
                    rootNote = note.note,
                    state = ListState.LOADED
                )
            )
        }
    }

    private fun createNote(text: String, parent: Note?) {
        val type = noteTypes.values.first { it.default }
        services.noteStorage.buildNewNote(
            type = type,
            text = text,
            parent = parent
        )
    }

    private suspend fun getChildNotesWithHierarchy(root: NoteWithChildren): Collection<Note> {
        val notTagTypes = noteTypes.values.filter { !it.tag }.map { it.id }.toList()
        if (!root.note.type.hierarchical) {
            return services.noteStorage.getNotes(
                NotesFilter(
                    typesToLoad = notTagTypes,
                    noteIdsForLoad = root.children
                ))
        }

        val rootTag = root.note.type.id
        val result = mutableSetOf<Note>()
        var nextNodeId: String? = null
        do {
            val node = if (nextNodeId == null) root else services.noteStorage.getNoteWithChildren(nextNodeId)
            val children = services.noteStorage.getNotes(
                NotesFilter(
                    typesToLoad = notTagTypes + rootTag,
                    noteIdsForLoad = node.children
                )
            )
            result.addAll(children.filter { it.type.id != rootTag })
            nextNodeId = children.find { it.type.id == rootTag }?.id

        } while (nextNodeId != null && nextNodeId != root.note.id)

        return result
    }
}