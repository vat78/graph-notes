package ru.vat78.notes.clients.android.data

suspend fun uploadTextInsertions(
    notes: List<Note>,
    dictionaryFunction: suspend (NotesFilter) -> List<DictionaryElement>,
    saveFunction: suspend (Note) -> Unit = {}
) : List<Note> {
    val tagIds = notes.flatMap{ it.textInsertions.keys }
    val filter = NotesFilter(tagIds)
    val tags = dictionaryFunction.invoke(filter).associateBy { it.id }
    return notes.map { fillNoteByTextInsertions(it, tags, saveFunction) }
}

private suspend fun fillNoteByTextInsertions(
    note: Note,
    tags: Map<String, DictionaryElement>,
    saveFunction: suspend (Note) -> Unit
): Note {
    var needToSave = false
    val filledInsertions = note.textInsertions.map { insertion ->
        if (tags[insertion.key] != null) {
            val filledTag = tags[insertion.key]!!
            needToSave = needToSave || insertion.value.caption != filledTag.caption
            filledTag
        } else {
            insertion.value
        }
    }.associateBy { it.id }

    val filledNote = note.copy(textInsertions = filledInsertions)
    if (needToSave) saveFunction.invoke(filledNote)
    return filledNote
}