package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.ui.ext.pmap
import java.time.ZonedDateTime
import java.util.*

@Immutable
data class DictionaryElement(
    val id: String,
    val type: NoteType,
    val caption: String = "",
    val color: Color = Color.Transparent,
) {
    constructor(note: Note): this(note.id, note.type, note.caption, note.color)
}

interface TagStorage {

    suspend fun getTagsByIds(ids: Collection<String>): Set<DictionaryElement>
    suspend fun getParentsByNote(noteId: String): Set<DictionaryElement>
    suspend fun findTagByCaption(caption: String): DictionaryElement?
}

suspend fun getTagById(tagId: String, tagStorage: TagStorage): DictionaryElement {
    return tagStorage.getTagsByIds(listOf(tagId)).first()
}

suspend fun getParentTagsByNote(noteId: String, tagStorage: TagStorage): Collection<DictionaryElement> {
    return tagStorage.getParentsByNote(noteId)
}

suspend fun getDirectlyLinkedTags(tags: Iterable<DictionaryElement>, tagStorage: TagStorage): Collection<DictionaryElement> {
    return tags
        .pmap { Pair(it, tagStorage.getParentsByNote(it.id)) }
        .asSequence()
        .map {
            val noteType = it.first.type
            if (noteType.hierarchical)
                it.second.filter { t -> t.type.id != noteType.id }.toSet()
            else it.second
        }
        .flatMap { it.asSequence() }
        .toSet()
}

suspend fun saveTag(tag: DictionaryElement,
                    tagStorage: TagStorage,
                    noteStorage: NoteStorage,
                    linkStorage: NoteLinkStorage,
                    wordStorage: WordStorage): ValidationResult<DictionaryElement> {
    if (!tag.type.tag) {
        return ValidationResult(null, R.string.tag_error_wrong_type)
    }
    if (tag.caption.isBlank()) {
        return ValidationResult(null, R.string.tag_error_empty_caption)
    }
    if (tag.id.isNotBlank()) {
        return ValidationResult(tag, null)
    }
    val tagInDb = tagStorage.findTagByCaption(tag.caption)
    if (tagInDb != null) {
        return ValidationResult(tagInDb, null)
    }

    val tagNote = Note(
        id = UUID.randomUUID().toString(),
        type =  tag.type,
        caption = tag.caption,
        start = generateTime(tag.type.defaultStart),
        finish = generateTime(tag.type.defaultFinish),
    )
    saveNote(NoteWithParents(tagNote, emptySet()), noteStorage, linkStorage, wordStorage)
    return ValidationResult(tag.copy(id = tagNote.id), null)
}