package ru.vat78.notes.clients.android.ui.screens.timeline

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
import ru.vat78.notes.clients.android.data.NoteWithParents
import ru.vat78.notes.clients.android.data.getBatchOfTextNotes
import ru.vat78.notes.clients.android.data.getBatchOfTextNotesByParent
import ru.vat78.notes.clients.android.data.getTagById
import ru.vat78.notes.clients.android.data.saveTag
import ru.vat78.notes.clients.android.data.searchTagSuggestions
import ru.vat78.notes.clients.android.data.tomorrow
import ru.vat78.notes.clients.android.ui.ext.analyzeTags
import ru.vat78.notes.clients.android.ui.ext.insertSuggestedTag
import ru.vat78.notes.clients.android.ui.ext.insertTags

class NoteListViewModel(
    private val appState: AppState,
) : BaseViewModel<NoteListState, NoteListEvent>(
    initialState = NoteListState(
        rootNote = null,
        caption = "",
        notes = sortedSetOf(),
        state = ListState.INIT,
        inputValue = TextFieldValue("")
    )
) {

    private val services
        get() = appState.context.services

    override fun sendEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.LoadNotes -> loadNotes(state.value)
            is NoteListEvent.LoadNotesByParent -> loadNotesByParent(event.parentId, state.value)
            is NoteListEvent.CreateNote -> createNote(event.text, state.value.rootNote, state.value.selectedSuggestions)
            is NoteListEvent.NewTextInput -> handleTextInput(state.value, event.textInput)
            is NoteListEvent.SelectSuggestion -> insertSuggestion(state.value, event.tag)
            is NoteListEvent.CreateNewTag -> createNewTag(state.value, event.tag)
            is NoteListEvent.CancelNewTag -> cancelNewTag(state.value)
            is NoteListEvent.ChangeNewTagType -> changeTypeOnNewTag(state.value, event.tag, event.type)
        }
    }

    private fun createNewTag(oldState: NoteListState, tag: DictionaryElement) {
        viewModelScope.launch {
            val savedTag = saveTag(tag, services.tagStorage, services.noteStorage, services.linkStorage, services.suggestionStorage)
            if (savedTag.data == null) {
                _state.emit(oldState.copy(error = savedTag.errorCode))
            } else {
                insertSuggestion(oldState.copy(newTag = null), savedTag.data)
            }
        }
    }

    private fun cancelNewTag(oldState: NoteListState) {
        _state.tryEmit(oldState.copy(newTag = null))
    }

    private fun changeTypeOnNewTag(oldState: NoteListState, tag: DictionaryElement, newType: NoteType) {
        val newTag = tag.copy(type = newType)
        _state.tryEmit(oldState.copy(newTag = newTag))
    }

    private fun insertSuggestion(oldState: NoteListState, tag: DictionaryElement) {
        if (tag.id.isBlank()) {
            _state.tryEmit(oldState.copy(newTag = tag))
            return
        }
        val text = oldState.inputValue.insertSuggestedTag(tag, NoteTypes.tagSymbols)
        val selections = oldState.selectedSuggestions + tag
        _state.tryEmit(oldState.copy(
            inputValue = text,
            selectedSuggestions = selections,
            suggestions = emptyList()
        ))
    }

    private fun handleTextInput(oldState: NoteListState, textInput: TextFieldValue) {
        val tagAnalyze = textInput.analyzeTags(NoteTypes.tagSymbols, oldState.inputValue.text)
        _state.tryEmit(oldState.copy(inputValue = tagAnalyze.third))
        if (tagAnalyze.second.last - tagAnalyze.second.first > 2) {
            viewModelScope.launch {
                val tagText = textInput.text.substring(tagAnalyze.second)
                val suggestions = searchTagSuggestions(tagText, NoteWithParents(Note(), oldState.selectedSuggestions), 5, services.suggestionStorage, services.tagStorage)
                _state.emit(_state.value.copy(suggestions = suggestions))
            }
        } else {
            _state.tryEmit(_state.value.copy(suggestions = emptyList()))
        }
    }

    private fun loadNotes(oldState: NoteListState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(
            NoteListState(
                rootNote = oldState.rootNote,
                caption = oldState.caption,
                notes = sortedSetOf(),
                state = ListState.LOADING,
                inputValue = oldState.inputValue
            )
        )
        viewModelScope.launch {
            getBatchOfTextNotes(tomorrow(), services.noteStorage, services.tagStorage) {
                if (it.isNotEmpty()) {
                    val notes = (_state.value.notes + it).toSortedSet(_state.value.sortingType.comparator)
                    _state.tryEmit(_state.value.copy(notes = notes, state = ListState.LOADED))
                }
            }
        }
    }

    private fun loadNotesByParent(parentId: String, oldState: NoteListState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(
            NoteListState(
                rootNote = oldState.rootNote,
                caption = oldState.caption,
                notes = sortedSetOf(),
                state = ListState.LOADING,
                inputValue = oldState.inputValue
            )
        )

        viewModelScope.launch {
            val rootNote = if (oldState.rootNote == null) {
                val note = getTagById(parentId,services.tagStorage)
                _state.emit(
                    NoteListState(
                        rootNote = note,
                        caption = note.caption,
                        notes = sortedSetOf(),
                        state = ListState.LOADING,
                        inputValue = oldState.inputValue
                    )
                )
                note
            } else {
                oldState.rootNote
            }

            getBatchOfTextNotesByParent(rootNote, tomorrow(), services.noteStorage, services.tagStorage, services.linkStorage) {
                if (it.isNotEmpty()) {
                    val notes = (_state.value.notes + it).toSortedSet(_state.value.sortingType.comparator)
                    _state.tryEmit(_state.value.copy(notes = notes, state = ListState.LOADED))
                }
            }
        }
    }

    private fun createNote(text: String, parent: DictionaryElement?, insertions: Set<DictionaryElement>) {
        val type = NoteTypes.types.values.first { it.default }
        val insertionMap = insertions.associateBy { it.caption }
        viewModelScope.launch {
            GlobalEventHandler.sendEvent(BuildNewNoteEvent(type, text.insertTags(insertionMap), parent, insertions))
        }
    }

}