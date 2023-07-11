package ru.vat78.notes.clients.android.ui.screens.notes

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteWithChildren
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.data.getWordsForSearch
import ru.vat78.notes.clients.android.data.uploadTextInsertions
import ru.vat78.notes.clients.android.ui.ext.analyzeTags
import ru.vat78.notes.clients.android.ui.ext.insertSuggestedTag
import ru.vat78.notes.clients.android.ui.ext.insertTags

class TagNotesViewModel(
    private val appState: AppState,
) : BaseViewModel<TagNotesUiState, TagNotesUiEvent>(
    initialState = TagNotesUiState(
        rootNote = Note(),
        notes = emptyList(),
        state = ListState.INIT,
        inputValue = TextFieldValue("")
    )
) {

    private val services
        get() = appState.context.services

    private val noteTypes
        get() = services.noteTypeStorage.types

    private val tagSymbols
        get() = noteTypes.values.map { it.symbol }.toSet()

    override fun sendEvent(event: TagNotesUiEvent) {
        when (event) {
            is TagNotesUiEvent.LoadData -> loadData(event.tagId, state.value)
            is TagNotesUiEvent.CreateTag -> createNote(event.caption, event.parent, state.value.selectedSuggestions)
            is TagNotesUiEvent.NewTextInput -> handleTextInput(state.value, event.textInput)
            is TagNotesUiEvent.SelectSuggestion -> insertSuggestion(state.value, event.tag)
        }
    }

    private fun loadData(tagId: String, oldState: TagNotesUiState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(state.value.copy(state = ListState.LOADING))

        viewModelScope.launch {
            val note = services.noteStorage.getNoteWithChildren(tagId)
            val values = getChildNotesWithHierarchy(note).sortedByDescending { it.finish }
            _state.emit(
                TagNotesUiState(
                    notes = values,
                    rootNote = note.note,
                    state = ListState.LOADED,
                    inputValue = oldState.inputValue
                )
            )
            val filledNotes = uploadTextInsertions(values, {services.noteStorage.getTags(it) }, { services.noteStorage.updateNote(it) })
            _state.emit(
                TagNotesUiState(
                    notes = filledNotes,
                    rootNote = note.note,
                    state = ListState.LOADED,
                    inputValue = oldState.inputValue
                )
            )
        }
    }


    private fun createNote(text: String, parent: Note?, insertions: Set<DictionaryElement>) {
        val type = noteTypes.values.first { it.default }
        val insertionMap = insertions.associateBy { it.caption }
        services.noteStorage.buildNewNote(
            type = type,
            text = text.insertTags(insertionMap),
            parent = parent,
            insertions = insertions
        )
    }

    private fun insertSuggestion(oldState: TagNotesUiState, tag: DictionaryElement) {
        val text = oldState.inputValue.insertSuggestedTag(tag, tagSymbols)
        val selections = oldState.selectedSuggestions + tag
        _state.tryEmit(oldState.copy(
            inputValue = text,
            selectedSuggestions = selections,
            suggestions = emptyList()
        ))
    }

    private fun handleTextInput(oldState: TagNotesUiState, textInput: TextFieldValue) {
        val tagAnalyze = textInput.analyzeTags(tagSymbols, oldState.inputValue.text)
        _state.tryEmit(oldState.copy(inputValue = tagAnalyze.third))
        if (tagAnalyze.second.last - tagAnalyze.second.first > 2) {
            viewModelScope.launch {
                val tagText = textInput.text.substring(tagAnalyze.second)
                Log.i("TagNotesUiState", "Search suggestions for tag text $tagText")
                val tagSymbol = tagText.first()
                val excludedTypes = if (tagSymbol == '#') emptyList() else noteTypes.values.filter { it.symbol != tagSymbol}.map { it.id }
                val hierarchical = oldState.selectedSuggestions.filter { it.type.hierarchical }.map { it.type.id }.toSet()
                val suggestions = services.tagSearchService.searchTagSuggestions(
                    words = getWordsForSearch(tagText.substring(1)),
                    excludedTypes = excludedTypes + hierarchical,
                    excludedTags = emptySet()
                )
                _state.emit(_state.value.copy(suggestions = suggestions))
            }
        } else {
            _state.tryEmit(_state.value.copy(suggestions = emptyList()))
        }
//        } else if (tagAnalyze.first.length > 4) {
//            viewModelScope.launch {
//                val tagText = tagAnalyze.first
//                Log.i("TagNotesUiState", "Search suggestions for simple text $tagText")
//                val suggestions = services.tagSearchService.searchTagSuggestions(
//                    words = getWordsForSearch(tagText.substring(1)),
//                    excludedTypes = emptyList(),
//                    excludedTags = oldState.selectedSuggestions.map { it.id }.toSet()
//                )
//                _state.emit(_state.value.copy(suggestions = suggestions))
//            }
//        }
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