package ru.vat78.notes.clients.android.ui.screens.tags

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NotesFilter

class TagsViewModel(
    private val appState: AppState,
) : BaseViewModel<TagsUiState, TagsUiEvent>(
    initialState = TagsUiState(
        tagType = NoteType(),
        caption = "",
        state = ListState.INIT
    )
) {

    private val services
        get() = appState.context.services

    private val noteTypes
        get() = services.noteTypeStorage.types

    override fun sendEvent(event: TagsUiEvent) {
        when (event) {
            is TagsUiEvent.LoadData -> loadData(event.type, event.tag, state.value)
            is TagsUiEvent.CreateTag -> createTag(event.type, event.caption, event.parent)
        }
    }

    private fun loadData(type: String, tag: String?, oldState: TagsUiState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(state.value.copy(state = ListState.LOADING))
        if (tag != null) {
            loadDataByTag(tag)
        } else {
            loadDataByType(type)
        }
    }

    private fun loadDataByType(type: String) {
        viewModelScope.launch {
            val mainType = noteTypes[type]!!
            val values = services.noteStorage.getNotes(
                NotesFilter(
                    typesToLoad = listOf(mainType.id),
                    onlyRoots = mainType.hierarchical
                )
            )
            _state.emit(
                TagsUiState(
                    tagType = mainType,
                    caption = mainType.name,
                    tags = values,
                    state = ListState.LOADED
                )
            )
        }
    }

    private fun loadDataByTag(tag: String) {
        viewModelScope.launch {
            val note = services.noteStorage.getNoteWithChildren(tag)
            val mainType = note.note.type
            val values = services.noteStorage.getNotes(
                NotesFilter(
                    typesToLoad = typesForFiltering(mainType, noteTypes.values),
                    noteIdsForLoad = note.children
                )).sortedBy { it.caption }
            _state.emit(
                TagsUiState(
                    tagType = mainType,
                    caption = note.note.caption,
                    tags = values,
                    rootNote = note.note,
                    state = ListState.LOADED
                )
            )
        }
    }

    private fun createTag(type: NoteType, text: String, parent: Note?) {
        services.noteStorage.buildNewNote(
            type = type,
            text = text,
            parent = parent
        )
    }

    private fun typesForFiltering(originType: NoteType, types: Collection<NoteType>) : List<String> = types.asSequence()
        .filter { (originType.hierarchical && originType.id == it.id) || (!originType.hierarchical && originType.tag == it.tag)}
        .map { it.id }
        .toList()
}