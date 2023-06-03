package ru.vat78.notes.clients.android.notes

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppEvent
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteWithParents

class NoteEditorUiReducer(
    initial: NoteEditorUiState,
    val contextHolder: ApplicationContext,
    val viewModelScope: CoroutineScope
) : Reducer<NoteEditorUiState, NotesEditorUiEvent>(initial) {

    override fun reduce(oldState: NoteEditorUiState, event: NotesEditorUiEvent) {
        when (event) {
            is NotesEditorUiEvent.ResetState -> {
                val noteTypes = contextHolder.services.noteTypeStorage.types
                setState(
                    NoteEditorUiState(
                        origin = oldState.origin,
                        changed = oldState.origin,
                        noteType = noteTypes[oldState.origin.note.type]!!,
                        availableTypes = noteTypes.values,
                        status = EditFormState.NEW
                    )
                )
            }

            is NotesEditorUiEvent.LoadNote -> {
                viewModelScope.launch {
                    val note = contextHolder.services.noteStorage.getNoteWithParents(event.uuid)
                    val noteTypes = contextHolder.services.noteTypeStorage.types
                    val state = if (event.uuid == "new") EditFormState.CHANGED else EditFormState.LOADED
                    setState(
                        NoteEditorUiState(
                            origin = note,
                            changed = note,
                            noteType = noteTypes[note.note.type]!!,
                            availableTypes = noteTypes.values,
                            status = state,
                        )
                    )
                }
            }

            is NotesEditorUiEvent.SaveNote -> {
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
                        contextHolder.services.noteStorage.saveNote(note, oldState.changed.parents)
                        contextHolder.riseEvent(AppEvent.NoteSaved(if (event.isNew) Note() else oldState.origin.note, note))
                    }
                    setState(
                        oldState.copy(status = EditFormState.CLOSED)
                    )
                }
            }

            is NotesEditorUiEvent.CancelChanges -> {
                viewModelScope.launch {
                    setState(
                        oldState.copy(status = EditFormState.CLOSED)
                    )
                }
            }

            is NotesEditorUiEvent.ChangeCaption -> {
                if (oldState.changed.note.caption == event.text) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note.copy(caption = event.text), oldState.changed.parents)
                        )
                    )
                }
            }

            is NotesEditorUiEvent.ChangeDescription -> {
                if (oldState.changed.note.description == event.text) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note.copy(description = event.text), oldState.changed.parents)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.ChangeType -> {
                if (oldState.noteType == event.type) return
                viewModelScope.launch {
                    Log.i("NoteEdit", "Changed type to ${event.type.id}")
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note.copy(type = event.type.id), oldState.changed.parents),
                            noteType = event.type
                        )
                    )
                }
            }

            is NotesEditorUiEvent.ChangeStart -> {
                if (oldState.changed.note.start == event.startTime) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note.copy(start = event.startTime), oldState.changed.parents),
                        )
                    )
                }
            }

            is NotesEditorUiEvent.ChangeFinish -> {
                if (oldState.changed.note.finish == event.finishTime) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note.copy(finish = event.finishTime), oldState.changed.parents),
                        )
                    )
                }
            }

            is NotesEditorUiEvent.AlignStartTime -> {
//                viewModelScope.launch {
//                    val start = noteStorage.getClosestTimeOrDefault(default = event.currentValue)
//                    setState(
//                        oldState.copy(
//                            status = EditFormState.CHANGED,
//                            note = oldState.note.copy(start = start)
//                        )
//                    )
//                }
            }

            is NotesEditorUiEvent.AddTag -> {
                viewModelScope.launch {
                    val newTags = oldState.changed.parents + event.newTag
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note, newTags),
                            suggestions = emptyList()
                        )
                    )
                }
            }

            is NotesEditorUiEvent.RemoveTag -> {
                viewModelScope.launch {
                    val newTags = oldState.changed.parents - event.tag
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            changed = NoteWithParents(oldState.changed.note, newTags),
                        )
                    )
                }
            }

            is NotesEditorUiEvent.RequestSuggestions -> {
                viewModelScope.launch {
                    val types = contextHolder.services.noteTypeStorage.types
                    val newSuggestions = contextHolder.services
                        .tagSearchService.searchTagSuggestions(event.text, oldState.changed) { types[it]!! }
                    setState(
                        oldState.copy(
                            suggestions = newSuggestions
                        )
                    )
                }
            }
        }
    }
}