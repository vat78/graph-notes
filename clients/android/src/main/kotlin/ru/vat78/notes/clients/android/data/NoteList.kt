package ru.vat78.notes.clients.android.data

import java.time.ZonedDateTime
import java.util.*

enum class SortingType(val comparator: Comparator<Note>, val groupFunction: (Note) -> String) {
    FINISH_TIME_DESC(
        compareBy<Note> { it.finish }.reversed(),
        {
            val day = it.finish.toLocalDate()
            if (day == java.time.LocalDate.now()) "Today" else day.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        }
    )
}

suspend fun getBatchOfTextNotes(
    fromTime: ZonedDateTime,
    noteStorage: NoteStorage,
    tagStorage: TagStorage,
    batchSize: Int = 100,
    onLoad: (List<Note>) -> Unit = {}
) {
    val notTagTypes = NoteTypes.types.values.filterNot { it.tag }.map { it.id }.toList()
    val notes = noteStorage.getNotesByTime(notTagTypes, fromTime, batchSize)
    onLoad.invoke(notes)
    GlobalEventHandler.sendEvent(ValidateNoteSuggestionEvent(notes, noteStorage, tagStorage, onLoad))
}

suspend fun getBatchOfTextNotesByParent(
    parent: DictionaryElement,
    fromTime: ZonedDateTime,
    noteStorage: NoteStorage,
    tagStorage: TagStorage,
    linkStorage: NoteLinkStorage,
    batchSize: Int = 100,
    onLoad: (List<Note>) -> Unit = {}
) {
    val notTagTypes = NoteTypes.types.values.filterNot { it.tag }.map { it.id }.toList()
    val parentIds = getHierarchyIds(parent, linkStorage)
    val ids = linkStorage.getChildrenIds(parentIds, notTagTypes)
    val notes = noteStorage.getNotesByIdAdTime(ids, notTagTypes, fromTime, batchSize)
    onLoad.invoke(notes)
    GlobalEventHandler.sendEvent(ValidateNoteSuggestionEvent(notes, noteStorage, tagStorage, onLoad))
}

suspend fun getTagNotesByParent(
    parent: DictionaryElement,
    noteStorage: NoteStorage,
    linkStorage: NoteLinkStorage,
    onLoad: (List<Note>) -> Unit = {}
) {
    val notTagTypes = listOf(parent.type.id)
    val ids = linkStorage.getChildrenIds(setOf(parent.id), notTagTypes)
    val notes = noteStorage.getNotesByIdAdTime(ids, notTagTypes, tomorrow(), 1000)
    onLoad.invoke(notes)
}

suspend fun getTagNotesByType(
    type: NoteType,
    noteStorage: NoteStorage,
    onLoad: (List<Note>) -> Unit = {}
) {
    val notTagTypes = listOf(type.id)
    val notes = noteStorage.getNotesByType(notTagTypes, 1000)
    onLoad.invoke(notes)
}

private suspend fun getHierarchyIds(root: DictionaryElement, linkStorage: NoteLinkStorage): Set<String> {
    val type = root.type
    if (!type.tag || !type.hierarchical) {
        return setOf(root.id)
    }
    val result = mutableSetOf(root.id)
    var idsForSearchChildren = setOf(root.id)
    while (idsForSearchChildren.isNotEmpty()) {
        idsForSearchChildren = linkStorage.getChildrenIds(idsForSearchChildren, listOf(type.id))
        result.addAll(idsForSearchChildren)
    }
    return result
}