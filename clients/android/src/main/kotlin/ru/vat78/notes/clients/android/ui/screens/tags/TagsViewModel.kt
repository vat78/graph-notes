package ru.vat78.notes.clients.android.ui.screens.tags

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.BuildNewNoteEvent
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.GlobalEventHandler
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypes
import ru.vat78.notes.clients.android.data.getTagById
import ru.vat78.notes.clients.android.data.getTagNotesByParent
import ru.vat78.notes.clients.android.data.getTagNotesByType
import ru.vat78.notes.clients.android.data.searchTagsByCaption

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
            val mainType = NoteTypes.getNoteTypeById(type)
            getTagNotesByType(mainType, services.noteStorage) { values ->
                val sorted = values.sortedBy { it.caption }
                _state.tryEmit(
                    TagsUiState(
                        tagType = mainType,
                        caption = mainType.name,
                        tags = sorted,
                        state = ListState.LOADED
                    )
                )
            }
        }
    }

    private fun loadDataByTag(tagId: String) {
        viewModelScope.launch {
            val tag = getTagById(tagId, services.tagStorage)
            val mainType = tag.type
            getTagNotesByParent(tag, services.noteStorage, services.linkStorage) { values ->
                val sorted = values.sortedBy { it.caption }
                _state.tryEmit(
                    TagsUiState(
                        tagType = mainType,
                        caption = tag.caption,
                        tags = sorted,
                        rootNote = tag,
                        state = ListState.LOADED
                    )
                )
            }
        }
    }

    private fun createTag(type: NoteType, text: String, parent: DictionaryElement?) {
        viewModelScope.launch {
            GlobalEventHandler.sendEvent(BuildNewNoteEvent(type, text, parent, emptySet()))
        }
    }

    private fun handleTextInput(oldState: TagsUiState, textInput: TextFieldValue) {
        if (textInput.text.length > 3) {
            viewModelScope.launch {
                val tagText = textInput.text
                val tags = searchTagsByCaption(tagText, oldState.tagType, oldState.rootNote, services.suggestionStorage, services.linkStorage, services.tagStorage)
                    .map { element -> Note(id = element.id, type = element.type, caption = element.caption, color = element.color) }
                _state.emit(_state.value.copy(tags = tags, filtered = true))
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