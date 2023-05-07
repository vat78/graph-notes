package ru.vat78.notes.clients.android.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.NoteTypeStructure
import ru.vat78.notes.clients.android.data.NotesStorage

class NoteEditorUiReducer(
    initial: NoteEditorUiState,
    val noteStorage: NotesStorage,
    val viewModelScope: CoroutineScope
) : Reducer<NoteEditorUiState, NotesEditorUiEvent>(initial) {

    override fun reduce(oldState: NoteEditorUiState, event: NotesEditorUiEvent) {
        when (event) {
            is NotesEditorUiEvent.ResetState -> {
                setState(
                    NoteEditorUiState(
                        note = oldState.note,
                        status = EditFormState.NEW
                    )
                )
            }

            is NotesEditorUiEvent.LoadNote -> {
                viewModelScope.launch {
                    val note = noteStorage.getOneNote(event.uuid)
                    val parent = noteStorage.getParent(event.uuid)
                    val links = noteStorage.getLinks(event.uuid)
                    val state = if (event.uuid == "new") EditFormState.CHANGED else EditFormState.LOADED
                    setState(
                        NoteEditorUiState(
                            note = note,
                            status = state,
                            parentValue = parent,
                            tags = links
                        )
                    )
                }
            }
            is NotesEditorUiEvent.SaveNote -> {
                viewModelScope.launch {
                    if (oldState.status == EditFormState.CHANGED) {
                        noteStorage.saveNote(oldState.note)
                        val structure = oldState.note.type.structure
                        if (structure == NoteTypeStructure.HIERARCHY) {
                            val links = oldState.parentValue?.let { setOf(it) } ?: emptySet()
                            noteStorage.saveLinks(oldState.note.uuid, links)
                        } else if (structure == NoteTypeStructure.INTERNAL_TAGS || structure == NoteTypeStructure.ANY_TAGS ) {
                            noteStorage.saveLinks(oldState.note.uuid, oldState.tags)
                        }
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
                if (oldState.note.caption == event.text) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = oldState.note.copy(caption = event.text)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.ChangeDescription -> {
                if (oldState.note.description == event.text) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = oldState.note.copy(description = event.text)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.ChangeType -> {
                if (oldState.note.type == event.type) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = oldState.note.copy(type = event.type)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.ChangeStart -> {
                if (oldState.note.start == event.startTime) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = oldState.note.copy(start = event.startTime)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.ChangeFinish -> {
                if (oldState.note.finish == event.finishTime) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = oldState.note.copy(finish = event.finishTime)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.AlignStartTime -> {
                viewModelScope.launch {
                    val start = noteStorage.getClosestTimeOrDefault(default = event.currentValue)
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = oldState.note.copy(start = start)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.AddTag -> {
                viewModelScope.launch {
                    if (event.onlyParent) {
                        setState(
                            oldState.copy(
                                status = EditFormState.CHANGED,
                                parentValue = event.newTag
                            )
                        )
                    } else {
                        val newTags = oldState.tags + event.newTag
                        setState(
                            oldState.copy(
                                status = EditFormState.CHANGED,
                                tags = newTags
                            )
                        )
                    }
                }
            }
            is NotesEditorUiEvent.RemoveTag -> {
                viewModelScope.launch {
                    val newTags = oldState.tags - event.tag
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            tags = newTags
                        )
                    )
                }
            }
        }
    }
}