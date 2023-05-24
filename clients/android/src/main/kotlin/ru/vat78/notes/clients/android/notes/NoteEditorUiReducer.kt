package ru.vat78.notes.clients.android.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.NoteWithLinks

class NoteEditorUiReducer(
    initial: NoteEditorUiState,
    val contextHolder: ApplicationContext,
    val viewModelScope: CoroutineScope
) : Reducer<NoteEditorUiState, NotesEditorUiEvent>(initial) {

    override fun reduce(oldState: NoteEditorUiState, event: NotesEditorUiEvent) {
        when (event) {
            is NotesEditorUiEvent.ResetState -> {
                setState(
                    NoteEditorUiState(
                        note = oldState.note,
                        noteType = oldState.noteType,
                        status = EditFormState.NEW
                    )
                )
            }

            is NotesEditorUiEvent.LoadNote -> {
                viewModelScope.launch {
                    val note = contextHolder.services.noteStorage.getNoteForEdit(event.uuid)
                    val noteType = contextHolder.services.noteTypeStorage.types.get(note.note.type)!!
                    val state = if (event.uuid == "new") EditFormState.CHANGED else EditFormState.LOADED
                    setState(
                        NoteEditorUiState(
                            note = note,
                            noteType = noteType,
                            status = state,
                        )
                    )
                }
            }

            is NotesEditorUiEvent.SaveNote -> {
//                viewModelScope.launch {
//                    if (oldState.status == EditFormState.CHANGED) {
//                        noteStorage.saveNote(oldState.note)
//                        val noteType = oldState.noteType
//                        if (noteType.isHierarchical) {
//                            val links = oldState.parentValue?.let { setOf(it) } ?: emptySet()
//                            noteStorage.saveLinks(oldState.note.uuid, links)
//                        } else {
//                            noteStorage.saveLinks(oldState.note.uuid, oldState.tags)
//                        }
//                    }
//                    setState(
//                        oldState.copy(status = EditFormState.CLOSED)
//                    )
//                }
            }

            is NotesEditorUiEvent.CancelChanges -> {
                viewModelScope.launch {
                    setState(
                        oldState.copy(status = EditFormState.CLOSED)
                    )
                }
            }

            is NotesEditorUiEvent.ChangeCaption -> {
                if (oldState.note.note.caption == event.text) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note.copy(caption = event.text), oldState.note.parents)
                        )
                    )
                }
            }

            is NotesEditorUiEvent.ChangeDescription -> {
                if (oldState.note.note.description == event.text) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note.copy(description = event.text), oldState.note.parents)
                        )
                    )
                }
            }
            is NotesEditorUiEvent.ChangeType -> {
                if (oldState.noteType == event.type) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note.copy(type = event.type.id), oldState.note.parents),
                            noteType = event.type
                        )
                    )
                }
            }

            is NotesEditorUiEvent.ChangeStart -> {
                if (oldState.note.note.start == event.startTime) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note.copy(start = event.startTime), oldState.note.parents),
                        )
                    )
                }
            }

            is NotesEditorUiEvent.ChangeFinish -> {
                if (oldState.note.note.finish == event.finishTime) return
                viewModelScope.launch {
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note.copy(finish = event.finishTime), oldState.note.parents),
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
                    val newTags = oldState.note.parents + event.newTag
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note, newTags),
                            suggestions = emptyList()
                        )
                    )
                }
            }

            is NotesEditorUiEvent.RemoveTag -> {
                viewModelScope.launch {
                    val newTags = oldState.note.parents - event.tag
                    setState(
                        oldState.copy(
                            status = EditFormState.CHANGED,
                            note = NoteWithLinks(oldState.note.note, newTags),
                        )
                    )
                }
            }

            is NotesEditorUiEvent.RequestSuggestions -> {
                viewModelScope.launch {
                    val types = contextHolder.services.noteTypeStorage.types
                    val newSuggestions = contextHolder.services
                        .tagSearchService.searchTagSuggestions(event.text, oldState.note.parents,) { types[it]!! }
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