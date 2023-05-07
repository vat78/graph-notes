package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NotesStorage

class NoteEditorViewModel(
    storage: NotesStorage
) : BaseViewModel<NoteEditorUiState, NotesEditorUiEvent>() {

    private val reducer = NoteEditorUiReducer(
        initial = NoteEditorUiState(
            note = Note(),
            status = EditFormState.NEW,
            parentValue = null,
            tags = setOf(),
        ),
        noteStorage = storage,
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<NoteEditorUiState>
        get() = reducer.state

    fun sendEvent(event: NotesEditorUiEvent) {
        reducer.sendEvent(event)
    }

    fun getModelForAutocomplete(initialText: String, typeFilter: NoteType? = null, timeFilter: Boolean = false): AutocompleteModel {
        return AutocompleteModel(
            initialText = initialText,
            storage = reducer.noteStorage,
            typeFilter = typeFilter,
            timeFilter = timeFilter)
    }

    class AutocompleteModel (
        initialText: String = "",
        val storage: NotesStorage,
        val typeFilter: NoteType? = null,
        val timeFilter: Boolean = false,
    ) {

        private val _suggestions: MutableList<DictionaryElement> = listOf<DictionaryElement>().toMutableStateList()
        val suggestions: List<DictionaryElement> = _suggestions

        private val _text = mutableStateOf(initialText)
        val text: String by _text

        fun requestSuggestions(text: String) {
            _suggestions.clear()
            _suggestions.addAll(storage.getAllByName(text, typeFilter, timeFilter))
            _text.value = text
        }
    }
}