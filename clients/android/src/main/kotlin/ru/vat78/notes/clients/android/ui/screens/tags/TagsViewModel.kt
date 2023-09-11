package ru.vat78.notes.clients.android.ui.screens.tags

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.data.getWordsForSearch

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

    private var stateBeforeFiltering: TagsUiState? = null

    override fun sendEvent(event: TagsUiEvent) {
        when (event) {
            is TagsUiEvent.LoadData -> loadData(event.type, event.tag, state.value)
            is TagsUiEvent.CreateTag -> createTag(event.type, event.caption, event.parent)
            is TagsUiEvent.NewTextInput -> handleTextInput(state.value, event.textInput)
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
            ).sortedBy { it.caption }
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

    private fun handleTextInput(oldState: TagsUiState, textInput: TextFieldValue) {
        if (textInput.text.length > 3) {
            viewModelScope.launch {
                val tagText = textInput.text
                Log.i("TagsViewModel", "Search suggestions for simple text $tagText and tag type ${oldState.tagType}")
                val suggestions = services.tagSearchService.searchTagSuggestions(
                    words = getWordsForSearch(tagText),
                    selectedType = oldState.tagType.id,
                    excludedTypes = emptyList(),
                    excludedTags = emptySet(),
                    maxCount =  0
                ).map { element -> Note(id = element.id, type = element.type, caption = element.caption, color = element.color) }
                _state.emit(_state.value.copy(tags = suggestions, filtered = true))
                if (!oldState.filtered) {
                    stateBeforeFiltering = oldState
                }
            }
        } else if (_state.value.filtered && stateBeforeFiltering != null) {
            val state = stateBeforeFiltering!!.copy(filtered = false)
            stateBeforeFiltering = null
            _state.tryEmit(state)
        }
    }

    private fun typesForFiltering(originType: NoteType, types: Collection<NoteType>) : List<String> = types.asSequence()
        .filter { (originType.hierarchical && originType.id == it.id) || (!originType.hierarchical && originType.tag == it.tag)}
        .map { it.id }
        .toList()
}