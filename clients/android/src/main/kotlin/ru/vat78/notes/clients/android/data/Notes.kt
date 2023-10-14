package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ru.vat78.notes.clients.android.R
import java.time.ZonedDateTime
import java.util.*

@Immutable
data class Note(
    val type: NoteType = NoteType(),
    val id: String = UUID.randomUUID().toString(),
    val caption: String = "",
    val color: Color = Color.Transparent,
    val description: String = "",
    val start: ZonedDateTime = currentTime(),
    val finish: ZonedDateTime = currentTime(),
    val root: Boolean = false,
    val textInsertions: Map<String, DictionaryElement> = emptyMap(),
    val lastUpdate: Long = currentTimestamp(),
    val deleted: Boolean = false
)

const val NEW_NOTE_ID = "new"

@Immutable
data class NoteWithParents(
    val note: Note,
    val parents: Set<DictionaryElement> = emptySet(),
)


interface NoteSyncStorage {
    suspend fun save(note: Note)
    suspend fun getNotesForSync(from: Long, to: Long): List<Note>
}

interface NoteStorage: NoteSyncStorage {
    suspend fun getById(id: String): Note?
    suspend fun updateSuggestions(notes: Collection<Note>)

    suspend fun getNotesByTime(types: List<String>, from: ZonedDateTime, count: Int): List<Note>
    suspend fun getNotesByIdAdTime(ids: Collection<String>, types: List<String>, from: ZonedDateTime, count: Int): List<Note>

    suspend fun getNotesByType(types: List<String>, count: Int): List<Note>

//    suspend fun getNotes(filter: NotesFilter): List<Note>
//    suspend fun getTags(filter: NotesFilter): List<DictionaryElement>
//    fun buildNewNote(type: NoteType, text: String, parent: Note? = null, insertions: Set<DictionaryElement> = emptySet())
//    suspend fun getNoteWithParents(uuid: String): NoteWithParents
//    suspend fun getNoteWithChildren(uuid: String): NoteWithChildren
//    suspend fun saveNote(note: Note, parents: Set<DictionaryElement>): Note
//    suspend fun persistNote(note: Note)

}

internal var _newNote: NoteWithParents? = null

suspend fun getNoteById(id: String, noteStorage: NoteStorage, tagStorage: TagStorage): NoteWithParents {
    if (id.isBlank() || NEW_NOTE_ID == id) {
        return _newNote!!
    }
    val note = noteStorage.getById(id)!!
    val parents = tagStorage.getParentsByNote(note.id)
    GlobalEventHandler.sendEvent(ValidateNoteSuggestionEvent(listOf(note), noteStorage, tagStorage))
    return NoteWithParents(note, parents)
}

suspend fun saveNote(note: NoteWithParents, noteStorage: NoteStorage, linkStorage: NoteLinkStorage, wordStorage: WordStorage) {
    val noteToSave = note.note.copy(lastUpdate = currentTimestamp())
    noteStorage.save(noteToSave)
    GlobalEventHandler.sendEvent(UpdateNoteLinksEvent(noteToSave.id, note.parents, linkStorage))
    if (noteToSave.type.tag) {
        GlobalEventHandler.sendEvent(UpdateTagWordsEvent(noteToSave, wordStorage))
    }
}

suspend fun validateNote(note: Note, parents: Set<DictionaryElement>, tagStorage: TagStorage): ValidationResult<NoteWithParents> {
    lateinit var validatedNote : Note
    if (note.type.tag) {
        if (note.caption == "") {
            return ValidationResult(errorCode = R.string.tag_error_empty_caption)
        }
        if (tagStorage.findTagByCaption(note.caption) != null) {
            return ValidationResult(errorCode = R.string.tag_error_already_exist)
        }
        val noteType = note.type
        val root = parents.none { it.type == noteType }
        validatedNote = note.copy(root = root)
    } else {
        if (note.description == "") {
            return ValidationResult(errorCode = R.string.note_error_empty_text)
        }
        validatedNote = note.copy(caption = "")
    }
    return ValidationResult(data = NoteWithParents(validatedNote, parents))
}

data class BuildNewNoteEvent(
    val type: NoteType,
    val text: String,
    val parent: DictionaryElement?,
    val insertions: Set<DictionaryElement>
): ApplicationEvent {
    override suspend fun handle() {
        val startTime = generateTime(type.defaultStart, ZonedDateTime::now)
        val finishTime = generateTime(type.defaultFinish, ZonedDateTime::now)
        val note: Note by lazy {
            if (type.tag) {
                Note(
                    type = type,
                    caption = text.trim(),
                    start = startTime,
                    finish = finishTime,
                )
            } else {
                Note(
                    type = type,
                    description = text.trim(),
                    start = startTime,
                    finish = finishTime,
                    textInsertions = insertions.associateBy { it.id }
                )
            }
        }
        _newNote = if (parent == null) {
            NoteWithParents(note, insertions)
        } else {
            NoteWithParents(note, insertions + parent)
        }
    }
}

enum class TimeDefault {
    START_OF_TIME, PREVIOUS_NOTE, NOW, NEXT_MONTH, NEXT_YEAR, END_OF_TIME,
}


