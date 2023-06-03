package ru.vat78.notes.clients.android.tags

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.Reducer
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.notes.ListState

class TagsUiReducer (
    initial: TagsUiState,
    val contextHolder: ApplicationContext,
    val viewModelScope: CoroutineScope
) : Reducer<TagsUiState, TagsUiEvent>(initial) {

    override fun reduce(oldState: TagsUiState, event: TagsUiEvent) {
        when (event) {
            is TagsUiEvent.LoadData -> {
                Log.i("TagsUiReducer", "Load data by event $event in state ${oldState.state}")
                setState(TagsUiState(
                    tagType = oldState.tagType,
                    caption = oldState.caption,
                    tags = oldState.tags,
                    noteTypes = oldState.noteTypes,
                    rootNote = oldState.rootNote,
                    state = ListState.LOADING
                ))
                viewModelScope.launch {
                    val noteTypes = contextHolder.services.noteTypeStorage.types
                    if (event.tag != null) {
                        val note = contextHolder.services.noteStorage.getNoteWithChildren(event.tag)
                        val mainType = noteTypes[note.note.type]!!
                        val values = contextHolder.services.noteStorage.getNotes(NotesFilter(
                            typesToLoad = typesForFiltering(mainType, noteTypes.values),
                            noteIdsForLoad = note.children
                        )).sortedBy { it.caption }
                        setState(TagsUiState(
                            tagType = mainType,
                            caption = note.note.caption,
                            tags = values,
                            noteTypes = noteTypes,
                            rootNote = note.note,
                            state = ListState.LOADED
                        ))
                    } else {
                        val mainType = noteTypes[event.type]!!
                        val values = contextHolder.services.noteStorage.getNotes(NotesFilter(
                            typesToLoad = listOf(mainType.id),
                            onlyRoots = mainType.hierarchical
                        ))
                        setState(TagsUiState(
                            tagType = mainType,
                            caption = mainType.name,
                            tags = values,
                            noteTypes = noteTypes,
                            state = ListState.LOADED
                        ))
                    }
                }
            }
            is TagsUiEvent.CreateTag -> {
                viewModelScope.launch {
                    contextHolder.services.noteStorage.buildNewNote(
                        type = event.type,
                        text = event.caption,
                        parent = event.parent
                    )
                }
            }
        }
    }
}

fun typesForFiltering(originType: NoteType, types: Collection<NoteType>) : List<String> = types.asSequence()
    .filter { (originType.hierarchical && originType.id == it.id) || (!originType.hierarchical && originType.tag == it.tag)}
    .map { it.id }
    .toList()