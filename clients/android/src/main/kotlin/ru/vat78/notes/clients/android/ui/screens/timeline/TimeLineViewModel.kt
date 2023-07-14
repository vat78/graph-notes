package ru.vat78.notes.clients.android.ui.screens.timeline

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.base.ListState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.data.generateTime
import ru.vat78.notes.clients.android.data.getWordsForSearch
import ru.vat78.notes.clients.android.data.uploadTextInsertions
import ru.vat78.notes.clients.android.ui.ext.analyzeTags
import ru.vat78.notes.clients.android.ui.ext.insertSuggestedTag
import ru.vat78.notes.clients.android.ui.ext.insertTags
import java.time.ZonedDateTime
import java.util.*

class TimeLineViewModel(
    private val appState: AppState,
) : BaseViewModel<TimeLineState, TimeLineEvent>(
    initialState = TimeLineState(
        caption = "",
        notes = emptyList(),
        state = ListState.INIT,
        inputValue = TextFieldValue("")
    )
) {

    private val services
        get() = appState.context.services
    val noteTypes
        get() = services.noteTypeStorage.types

    private val tagSymbols
        get() = noteTypes.values.map { it.symbol }.toSet()

    override fun sendEvent(event: TimeLineEvent) {
        when (event) {
            is TimeLineEvent.LoadNotes -> loadNotes(state.value)
            is TimeLineEvent.CreateNote -> createNote(event.text, state.value.selectedSuggestions)
            is TimeLineEvent.NewTextInput -> handleTextInput(state.value, event.textInput)
            is TimeLineEvent.SelectSuggestion -> insertSuggestion(state.value, event.tag)
            is TimeLineEvent.CreateNewTag -> createNewTag(state.value, event.tag)
            is TimeLineEvent.CancelNewTag -> cancelNewTag(state.value)
            is TimeLineEvent.ChangeNewTagType -> changeTypeOnNewTag(state.value, event.tag, event.type)
        }
    }

    private fun createNewTag(oldState: TimeLineState, tag: DictionaryElement) {
        val newTag = Note(
            id = UUID.randomUUID().toString(),
            type =  tag.type,
            caption = tag.caption,
            start = generateTime(tag.type.defaultStart, { ZonedDateTime.now() }),
            finish = generateTime(tag.type.defaultFinish, { ZonedDateTime.now() }),
        )
        viewModelScope.launch {
            val savedNote = services.noteStorage.saveNote(newTag, emptySet())
            insertSuggestion(oldState.copy(newTag = null), DictionaryElement(savedNote))
        }
    }

    private fun cancelNewTag(oldState: TimeLineState) {
        _state.tryEmit(oldState.copy(newTag = null))
    }

    private fun changeTypeOnNewTag(oldState: TimeLineState, tag: DictionaryElement, newType: NoteType) {
        val newTag = tag.copy(type = newType)
        _state.tryEmit(oldState.copy(newTag = newTag))
    }

    private fun insertSuggestion(oldState: TimeLineState, tag: DictionaryElement) {
        if (tag.id.isBlank()) {
            _state.tryEmit(oldState.copy(newTag = tag))
            return
        }
        val text = oldState.inputValue.insertSuggestedTag(tag, tagSymbols)
        val selections = oldState.selectedSuggestions + tag
        _state.tryEmit(oldState.copy(
            inputValue = text,
            selectedSuggestions = selections,
            suggestions = emptyList()
        ))
    }

    private fun handleTextInput(oldState: TimeLineState, textInput: TextFieldValue) {
        val tagAnalyze = textInput.analyzeTags(tagSymbols, oldState.inputValue.text)
        _state.tryEmit(oldState.copy(inputValue = tagAnalyze.third))
        if (tagAnalyze.second.last - tagAnalyze.second.first > 2) {
            viewModelScope.launch {
                val tagText = textInput.text.substring(tagAnalyze.second)
                val tagSymbol = tagText.first()
                val excludedTypes =
                    if (tagSymbol == '#') emptyList() else noteTypes.values.filter { it.symbol != tagSymbol }
                        .map { it.id }
                val hierarchical =
                    oldState.selectedSuggestions.filter { it.type.hierarchical }.map { it.type.id }.toSet()
                Log.i(
                    "TimeLineViewModel",
                    "Search suggestions for tag text $tagText with excluded $excludedTypes by tag $tagSymbol"
                )
                val textForSearch = tagText.substring(1)
                val suggestions = services.tagSearchService.searchTagSuggestions(
                    words = getWordsForSearch(textForSearch),
                    excludedTypes = excludedTypes + hierarchical,
                    excludedTags = emptySet()
                ) + newDictionaryElementForSuggestion(tagSymbol, textForSearch)
                _state.emit(_state.value.copy(suggestions = suggestions))
            }
        } else {
            _state.tryEmit(_state.value.copy(suggestions = emptyList()))
        }
//        } else if (tagAnalyze.first.length > 4) {
//            viewModelScope.launch {
//                val tagText = tagAnalyze.first
//                Log.i("TimeLineViewModel", "Search suggestions for simple text $tagText")
//                val suggestions = services.tagSearchService.searchTagSuggestions(
//                    words = getWordsForSearch(tagText.substring(1)),
//                    excludedTypes = emptyList(),
//                    excludedTags = oldState.selectedSuggestions.map { it.id }.toSet()
//                )
//                _state.emit(_state.value.copy(suggestions = suggestions))
//            }
//        }
    }

    private fun newDictionaryElementForSuggestion(tagSymbol: Char, tagText: String) : DictionaryElement {
        val tagType = noteTypes.values.first { it.symbol == tagSymbol }
        return DictionaryElement(
            id = "",
            type = tagType,
            caption = tagText
        )
    }

    private fun loadNotes(oldState: TimeLineState) {
        if (oldState.state == ListState.LOADING) return
        _state.tryEmit(
            TimeLineState(
                caption = oldState.caption,
                notes = emptyList(),
                state = ListState.LOADING,
                inputValue = oldState.inputValue
            )
        )
        viewModelScope.launch {
            val notes = services.noteStorage.getNotes(NotesFilter (
                typesToLoad = noteTypes.values.filter { !it.tag }.map { it.id }
            )).sortedByDescending { it.finish }
            _state.emit(
                TimeLineState(
                    caption = oldState.caption,
                    notes = notes,
                    state = ListState.LOADED,
                    inputValue = oldState.inputValue
                )
            )
            val filledNotes = uploadTextInsertions(notes, {services.noteStorage.getTags(it) }, { services.noteStorage.updateNote(it) })
            _state.emit(
                TimeLineState(
                    caption = oldState.caption,
                    notes = filledNotes,
                    state = ListState.LOADED,
                    inputValue = oldState.inputValue
                )
            )
        }
    }

    private fun createNote(text: String, insertions: Set<DictionaryElement>) {
        val type = noteTypes.values.first { it.default }
        val insertionMap = insertions.associateBy { it.caption }
        services.noteStorage.buildNewNote(
            type = type,
            text = text.insertTags(insertionMap),
            insertions = insertions
        )
    }

}