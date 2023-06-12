package ru.vat78.notes.clients.android.ui.screens.editor

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppEvent
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteWithParents

class NoteEditorViewModel(
    private val appState: AppState,
) : BaseViewModel<NoteEditorUiState, NotesEditorUiEvent>(
    initialState = NoteEditorUiState(
        origin = NoteWithParents(Note(NoteType()), emptySet()),
        changed = NoteWithParents(Note(NoteType()), emptySet()),
        noteType = NoteType(),
        status = EditFormState.NEW,
        availableTypes = emptyList()
    )
) {

    private val services
        get() = appState.context.services

    val noteTypes
        get() = services.noteTypeStorage.types

    override fun sendEvent(event: NotesEditorUiEvent) {
        when (event) {
            is NotesEditorUiEvent.ResetState -> resetState(state.value)
            is NotesEditorUiEvent.LoadNote -> loadNote(event.uuid, state.value)
            is NotesEditorUiEvent.SaveNote -> saveNote(event.isNew, state.value)
            is NotesEditorUiEvent.CancelChanges -> _state.tryEmit(state.value.copy(status = EditFormState.CLOSED))
            is NotesEditorUiEvent.ChangeEvent -> changeNote(event, state.value)
            is NotesEditorUiEvent.AlignStartTime -> {}
            is NotesEditorUiEvent.AddTag -> addTag(event.newTag, state.value)
            is NotesEditorUiEvent.RemoveTag -> removeTag(event.tag, state.value)
            is NotesEditorUiEvent.RequestSuggestions -> loadSuggestions(event.text, state.value)
        }
    }

    private fun resetState(oldState : NoteEditorUiState) {
        _state.tryEmit(
            NoteEditorUiState(
                origin = oldState.origin,
                changed = oldState.origin,
                availableTypes = noteTypes.values,
                noteType = oldState.origin.note.type,
                status = EditFormState.NEW
            )
        )
    }

    private fun loadNote(uuid: String, oldState: NoteEditorUiState) {
        if (oldState.status != EditFormState.NEW) return
        viewModelScope.launch {
            val note = services.noteStorage.getNoteWithParents(uuid)
            val state = if (uuid == "new") EditFormState.CHANGED else EditFormState.LOADED
            _state.emit(
                NoteEditorUiState(
                    origin = note,
                    changed = note,
                    noteType = note.note.type,
                    status = state,
                    availableTypes = noteTypes.values
                )
            )
        }
    }

    private fun saveNote(isNew: Boolean, oldState: NoteEditorUiState) {
        viewModelScope.launch {
            if (oldState.status == EditFormState.CHANGED) {
                lateinit var note : Note
                if (oldState.noteType.tag) {
                    if (oldState.changed.note.caption == "") {
                        // ToDo: implement error
                    }
                    val noteType = oldState.changed.note.type
                    val notRoot = oldState.changed.parents.any { it.type == noteType }
                    note = oldState.changed.note.copy(root = !notRoot)
                } else {
                    if (oldState.changed.note.description == "") {
                        // ToDo: implement error
                    }
                    note = oldState.changed.note.copy(caption = "")
                }
                services.noteStorage.saveNote(note, oldState.changed.parents)
                appState.context.riseEvent(AppEvent.NoteSaved(if (isNew) Note() else oldState.origin.note, note))
            }
            _state.emit(
                oldState.copy(status = EditFormState.CLOSED)
            )
        }
    }

    private fun changeNote(changeEvent: NotesEditorUiEvent.ChangeEvent, oldState: NoteEditorUiState) {
        val changedNote = when (changeEvent) {
            is NotesEditorUiEvent.ChangeEvent.ChangeCaption -> {
                if (oldState.changed.note.caption == changeEvent.text) return
                oldState.changed.note.copy(caption = changeEvent.text)
            }

            is NotesEditorUiEvent.ChangeEvent.ChangeDescription -> {
                if (oldState.changed.note.description == changeEvent.text) return
                oldState.changed.note.copy(description = changeEvent.text)
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

        _state.tryEmit(
            NoteEditorUiState(
                origin = oldState.origin,
                status = EditFormState.CHANGED,
                changed = NoteWithParents(changedNote, oldState.changed.parents),
                noteType = oldState.noteType,
                availableTypes = noteTypes.values,
                suggestions = emptyList()
            )
        )
    }

    private fun addTag(tag: DictionaryElement, oldState: NoteEditorUiState) {
        val newTags = oldState.changed.parents + tag
        _state.tryEmit(
            oldState.copy(
                status = EditFormState.CHANGED,
                changed = NoteWithParents(oldState.changed.note, newTags),
                suggestions = emptyList()
            )
        )
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
            val newSuggestions = services
                .tagSearchService.searchTagSuggestions(text, oldState.changed)
            _state.emit(
                oldState.copy(
                    suggestions = newSuggestions
                )
            )
        }
    }
}