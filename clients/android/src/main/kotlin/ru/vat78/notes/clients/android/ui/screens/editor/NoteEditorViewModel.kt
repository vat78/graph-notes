package ru.vat78.notes.clients.android.ui.screens.editor

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.NEW_NOTE_ID
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypes
import ru.vat78.notes.clients.android.data.NoteWithParents
import ru.vat78.notes.clients.android.data.getDirectlyLinkedTags
import ru.vat78.notes.clients.android.data.getNoteById
import ru.vat78.notes.clients.android.data.saveNote
import ru.vat78.notes.clients.android.data.saveTag
import ru.vat78.notes.clients.android.data.searchTagSuggestions
import ru.vat78.notes.clients.android.data.validateNote
import ru.vat78.notes.clients.android.ui.ext.analyzeTags
import ru.vat78.notes.clients.android.ui.ext.insertCaptions
import ru.vat78.notes.clients.android.ui.ext.insertSuggestedTag
import ru.vat78.notes.clients.android.ui.ext.insertTags

class NoteEditorViewModel(
    private val appState: AppState,
) : BaseViewModel<NoteEditorUiState, NotesEditorUiEvent>(
    initialState = NoteEditorUiState(
        origin = NoteWithParents(Note(NoteType()), emptySet()),
        changed = NoteWithParents(Note(NoteType()), emptySet()),
        noteType = NoteType(),
        status = EditFormState.NEW,
        descriptionFocus = DescriptionFocusState.HIDE,
        descriptionTextValue = TextFieldValue(""),
        availableTypes = emptyList()
    )
) {

    private val services
        get() = appState.context.services

    override fun sendEvent(event: NotesEditorUiEvent) {
        when (event) {
            is NotesEditorUiEvent.ResetState -> resetState(state.value)
            is NotesEditorUiEvent.LoadNote -> loadNote(event.uuid, state.value)
            is NotesEditorUiEvent.SaveNote -> validateAndSaveNote(state.value)
            is NotesEditorUiEvent.CancelError -> _state.tryEmit(state.value.copy(status = EditFormState.CHANGED, errorMessage = null))
            is NotesEditorUiEvent.CancelChanges -> _state.tryEmit(state.value.copy(status = EditFormState.CLOSED))
            is NotesEditorUiEvent.ChangeEvent -> changeNote(event, state.value)
            is NotesEditorUiEvent.AlignStartTime -> {}
            is NotesEditorUiEvent.AddTag -> addTag(event.newTag, state.value)
            is NotesEditorUiEvent.AddChildTags -> loadAndAddTags(event.mainTags, state.value)
            is NotesEditorUiEvent.RemoveTag -> removeTag(event.tag, state.value)
            is NotesEditorUiEvent.RequestSuggestions -> loadSuggestions(event.text, state.value)
            is NotesEditorUiEvent.ChangeDescriptionFocus -> changeDescriptionFocus(event.focus, state.value)
            is NotesEditorUiEvent.CreateNewTag -> createNewTag(state.value, event.tag)
            is NotesEditorUiEvent.CancelNewTag -> cancelNewTag(state.value)
            is NotesEditorUiEvent.ChangeNewTagType -> changeTypeOnNewTag(state.value, event.tag, event.type)
        }
    }

    private fun resetState(oldState : NoteEditorUiState) {
        _state.tryEmit(
            NoteEditorUiState(
                origin = oldState.origin,
                changed = oldState.origin,
                availableTypes = NoteTypes.types.values,
                noteType = oldState.origin.note.type,
                status = EditFormState.NEW,
                descriptionFocus = DescriptionFocusState.HIDE,
                descriptionTextValue = TextFieldValue(oldState.origin.note.description)
            )
        )
    }

    private fun loadNote(uuid: String, oldState: NoteEditorUiState) {
        if (oldState.status != EditFormState.NEW) return
        viewModelScope.launch {
            val note = getNoteById(uuid, services.NoteRepository(), services.tagStorage)
            val state = if (uuid == NEW_NOTE_ID) EditFormState.CHANGED else EditFormState.LOADED
            _state.emit(
                NoteEditorUiState(
                    origin = note,
                    changed = note,
                    noteType = note.note.type,
                    status = state,
                    availableTypes = NoteTypes.types.values,
                    descriptionFocus = DescriptionFocusState.HIDE,
                    descriptionTextValue = TextFieldValue(note.note.description)
                )
            )
            if (uuid == NEW_NOTE_ID) {
                sendEvent(NotesEditorUiEvent.AddChildTags(note.parents))
            }
        }
    }

    private fun validateAndSaveNote(oldState: NoteEditorUiState) {
        viewModelScope.launch {
            if (oldState.status == EditFormState.CHANGED) {
                val validationResult = validateNote(oldState.changed.note, oldState.changed.parents, services.tagStorage)
                if (validationResult.data != null) {
                    saveNote(validationResult.data, services.noteStorage, services.linkStorage, services.suggestionStorage)
                    _state.emit(
                        oldState.copy(status = EditFormState.CLOSED)
                    )
                } else {
                    _state.emit(
                        oldState.copy(status = EditFormState.ERROR, errorMessage = validationResult.errorCode)
                    )
                }
            }
        }
    }

    private fun changeNote(changeEvent: NotesEditorUiEvent.ChangeEvent, oldState: NoteEditorUiState) {
        val changedNote = when (changeEvent) {
            is NotesEditorUiEvent.ChangeEvent.ChangeCaption -> {
                if (oldState.changed.note.caption == changeEvent.text) return
                oldState.changed.note.copy(caption = changeEvent.text)
            }

            is NotesEditorUiEvent.ChangeEvent.ChangeDescription -> {
                oldState.changed.note.copy(description = changeEvent.text.text.insertTags(oldState.changed.parents.associateBy { it.caption }))
            }

            is NotesEditorUiEvent.ChangeEvent.ChangeType -> {
                if (oldState.noteType == changeEvent.type) return
                oldState.changed.note.copy(type = changeEvent.type)
            }

            is NotesEditorUiEvent.ChangeEvent.ChangeStart -> {
                if (oldState.changed.note.start == changeEvent.startTime) return
                oldState.changed.note.copy(start = changeEvent.startTime)
            }

            is NotesEditorUiEvent.ChangeEvent.ChangeFinish -> {
                if (oldState.changed.note.finish == changeEvent.finishTime) return
                oldState.changed.note.copy(finish = changeEvent.finishTime)
            }
        }

        if (changeEvent is NotesEditorUiEvent.ChangeEvent.ChangeDescription) {
            handleTextInput(changedNote, oldState, changeEvent.text)
        } else {
            _state.tryEmit(
                NoteEditorUiState(
                    origin = oldState.origin,
                    status = EditFormState.CHANGED,
                    changed = NoteWithParents(changedNote, oldState.changed.parents),
                    noteType = oldState.noteType,
                    availableTypes = NoteTypes.types.values,
                    suggestions = emptyList(),
                    descriptionFocus = oldState.descriptionFocus,
                    descriptionTextValue = oldState.descriptionTextValue
                    )
            )
        }
    }

    private fun addTag(tag: DictionaryElement, oldState: NoteEditorUiState) {
        if (tag.id.isBlank()) {
            _state.tryEmit(oldState.copy(newTag = tag))
            return
        }
        val newTags = oldState.changed.parents + tag
        if (oldState.descriptionFocus == DescriptionFocusState.FOCUSED) {
            val text = oldState.descriptionTextValue.insertSuggestedTag(tag, NoteTypes.tagSymbols)
            val note = oldState.changed.note.copy(textInsertions = newTags.associateBy{ it.id})
            _state.tryEmit(
                oldState.copy(
                    status = EditFormState.CHANGED,
                    changed = NoteWithParents(note, newTags),
                    descriptionTextValue = text,
                    suggestions = emptyList()
                )
            )
            sendEvent(NotesEditorUiEvent.ChangeEvent.ChangeDescription(text))
        } else {
            _state.tryEmit(
                oldState.copy(
                    status = EditFormState.CHANGED,
                    changed = NoteWithParents(oldState.changed.note, newTags),
                    suggestions = emptyList()
                )
            )
        }
        sendEvent(NotesEditorUiEvent.AddChildTags(listOf(tag)))
    }

    private fun removeTag(tag: DictionaryElement, oldState: NoteEditorUiState) {
        val newTags = oldState.changed.parents - tag
        _state.tryEmit(
            oldState.copy(
                status = EditFormState.CHANGED,
                changed = NoteWithParents(oldState.changed.note, newTags),
                suggestions = emptyList()
            )
        )
    }

    private fun loadSuggestions(text: String, oldState: NoteEditorUiState) {
        viewModelScope.launch {
            val newSuggestions = searchTagSuggestions(text, oldState.changed, 5, services.suggestionStorage, services.tagStorage)
            _state.emit(
                oldState.copy(
                    suggestions = newSuggestions
                )
            )
        }
    }

    private fun changeDescriptionFocus(newValue: DescriptionFocusState, oldState: NoteEditorUiState) {
        if (newValue == oldState.descriptionFocus) return
        Log.i("NoteEditorViewModel", "New description focus value: $newValue")

        when (newValue) {
            DescriptionFocusState.HIDE -> if (oldState.descriptionFocus == DescriptionFocusState.FOCUSED) {
                _state.tryEmit(
                    oldState.copy(
                        descriptionFocus = DescriptionFocusState.HIDE,
                    )
                )
            }

            DescriptionFocusState.SHOW -> if (oldState.descriptionFocus == DescriptionFocusState.HIDE) {
                _state.tryEmit(oldState.copy(
                    descriptionFocus = DescriptionFocusState.SHOW,
                    descriptionTextValue = formatDescriptionForEdit(oldState.changed)
                ))
            }

            DescriptionFocusState.FOCUSED -> if (oldState.descriptionFocus == DescriptionFocusState.SHOW) {
                _state.tryEmit(oldState.copy(
                    descriptionFocus = DescriptionFocusState.FOCUSED,
                ))
            }

        }
    }


    private fun loadAndAddTags(mainTags: Iterable<DictionaryElement>, oldState: NoteEditorUiState) {
        viewModelScope.launch {
            val additionalTags = getDirectlyLinkedTags(mainTags, services.tagStorage)
            val newTags = oldState.changed.parents + additionalTags
            _state.emit(oldState.copy(
                changed = oldState.changed.copy(parents = newTags)
            ))
        }
    }

    private fun formatDescriptionForEdit(note: NoteWithParents): TextFieldValue {
        val insertionMap = note.parents.associateBy { it.id }
        return TextFieldValue(note.note.description.insertCaptions(insertionMap))
    }

    private fun handleTextInput(changedNote: Note, oldState: NoteEditorUiState, textInput: TextFieldValue) {
        val tagAnalyze = textInput.analyzeTags(NoteTypes.tagSymbols, oldState.descriptionTextValue.text)
        _state.tryEmit(oldState.copy(
            changed = NoteWithParents(changedNote, oldState.changed.parents),
            descriptionTextValue = tagAnalyze.third,
            status = EditFormState.CHANGED
        ))
        if (tagAnalyze.second.last - tagAnalyze.second.first > 2) {
            viewModelScope.launch {
                val tagText = textInput.text.substring(tagAnalyze.second)
                val suggestions = searchTagSuggestions(tagText, oldState.changed, 5, services.suggestionStorage, services.tagStorage)
                _state.emit(_state.value.copy(suggestions = suggestions))
            }
        } else {
            _state.tryEmit(_state.value.copy(suggestions = emptyList()))
        }
//        } else if (tagAnalyze.first.length > 4) {
//            viewModelScope.launch {
//                val tagText = tagAnalyze.first
//                Log.i("NoteEditorViewModel", "Search suggestions for simple text $tagText")
//                val suggestions = services.tagSearchService.searchTagSuggestions(
//                    words = getWordsForSearch(tagText.substring(1)),
//                    excludedTypes = emptyList(),
//                    excludedTags = oldState.changed.parents.map { it.id }.toSet()
//                )
//                _state.emit(_state.value.copy(suggestions = suggestions))
//            }
//        }
    }

    private fun createNewTag(oldState: NoteEditorUiState, tag: DictionaryElement) {
        viewModelScope.launch {
            val savedTag = saveTag(tag, services.tagStorage, services.noteStorage, services.linkStorage, services.suggestionStorage)
            if (savedTag.data == null) {
                // ToDo: add alert
            } else {
                addTag(savedTag.data, oldState.copy(newTag = null))
            }
        }
    }

    private fun cancelNewTag(oldState: NoteEditorUiState) {
        _state.tryEmit(oldState.copy(newTag = null))
    }

    private fun changeTypeOnNewTag(oldState: NoteEditorUiState, tag: DictionaryElement, newType: NoteType) {
        val newTag = tag.copy(type = newType)
        _state.tryEmit(oldState.copy(newTag = newTag))
    }
}