package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteStorage
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.NoteWithChildren
import ru.vat78.notes.clients.android.data.NoteWithParents
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.generateTime
import ru.vat78.notes.clients.android.ui.ext.pmap
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class NoteRepository (
    private val user: User,
    private val noteTypeRepository: NoteTypeStorage,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteStorage {

    private val noteTypes
        get() = noteTypeRepository.types

    private var newNote: NoteWithParents? = null

    override suspend fun getNotes(filter: NotesFilter): List<Note> {
        Log.i("NoteRepository", "Request for notes with filter $filter")
        if (filter.noteIdsForLoad?.isEmpty() == true) {
            return emptyList()
        } else if ((filter.noteIdsForLoad?.size ?: 0) > 30) {
            val ids = filter.noteIdsForLoad!!
            return ids.chunked(30)
                .pmap{ getNotes(filter.copy(noteIdsForLoad = it))}
                .asSequence()
                .flatMap { it.asSequence() }
                .toList()
        }
        val result = withContext(Dispatchers.IO) {
            var query: Query = firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
            if (filter.typesToLoad?.isNotEmpty() == true) {
                Log.i("NoteRepository", "Include types ${filter.typesToLoad}")
                query = query.whereIn("type", filter.typesToLoad)
            }
            if (filter.typesToExclude?.isNotEmpty() == true) {
                Log.i("NoteRepository", "Exclude types ${filter.typesToExclude}")
                query = query.whereNotIn("type", filter.typesToExclude)
            }
            if (filter.noteIdsForLoad != null) {
                Log.i("NoteRepository", "Get ids ${filter.noteIdsForLoad}")
                query = query.whereIn("id", filter.noteIdsForLoad)
            }
            if (filter.onlyRoots) {
                Log.i("NoteRepository", "Select only root elements")
                query = query.whereEqualTo("root", true)
            }
            return@withContext query
                .limit(100)
                .orderBy("finish")
                .get()
                .await()
                .map {doc -> doc.toNote(noteTypes)}
        }
        Log.i("NoteRepository", "Find ${result.size} elements")
        return result
    }

    override fun buildNewNote(type: NoteType, text: String, parent: Note?, insertions: Set<DictionaryElement>) {
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
        newNote = if (parent == null) {
            NoteWithParents(note, insertions)
        } else {
            NoteWithParents(note, insertions + DictionaryElement(parent))
        }
    }

    override suspend fun getNoteWithParents(uuid: String): NoteWithParents {
        if (uuid == "new") {
            return newNote ?: throw Exception("New note not created")
        }
        val parents = getParentLinks(uuid).toSet()
        val note = getNote(uuid).copy(textInsertions = parents.associateBy { it.id })
        return NoteWithParents(note, parents)
    }

    override suspend fun getNoteWithChildren(uuid: String): NoteWithChildren {
        return NoteWithChildren(getNote(uuid), getChildrenIds(uuid))
    }

    override suspend fun getTags(filter: NotesFilter): List<DictionaryElement> {
        return getNotes(filter)
            .map { DictionaryElement(id = it.id, type = it.type, caption = it.caption, color = it.color) }
    }

    override suspend fun updateNote(note: Note) {
        Log.i("NoteRepository", "Update note ${note.id}")
        withContext(Dispatchers.IO) {
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(note.id)
                .set(note.toMap(), SetOptions.merge())
        }
    }

    override suspend fun saveNote(note: Note, parents: Set<DictionaryElement>): Note {
        Log.i("NoteRepository", "Save note ${note.id} with its parent links")
        return withContext(Dispatchers.IO) {
            if (note.type.tag) {
                val savedNote = getNoteByCaption(note.caption)
                if (savedNote != null) return@withContext savedNote
            }
            val parentsFromDb = getParentIds(note.id).toSet()
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(note.id)
                .set(note.toMap(), SetOptions.merge())

            val parentIds = parents.map { it.id }.toSet()
            val newLinks = parentIds - parentsFromDb
            val forDeletion = parentsFromDb - parentIds

            firestore.runBatch { batch ->
                forDeletion.forEach {
                    deleteLink(batch, note.id, it)
                }
            }

            firestore.runBatch { batch ->
                newLinks.forEach {
                    insertLink(batch, note.id, it)
                }
            }
            return@withContext note
        }
    }

    private fun deleteLink(batch: WriteBatch, childNoteId: String, parentNoteId: String) {
        batch.delete(
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(childNoteId)
                .collection(PARENT_LINKS_COLLECTION)
                .document(parentNoteId)
        )
        batch.delete(
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(parentNoteId)
                .collection(CHILD_LINKS_COLLECTION)
                .document(childNoteId)
        )
    }

    private fun insertLink(batch: WriteBatch, childNoteId: String, parentNoteId: String) {
        batch.set(
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(childNoteId)
                .collection(PARENT_LINKS_COLLECTION)
                .document(parentNoteId), mapOf("id" to parentNoteId)
        )
        batch.set(
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(parentNoteId)
                .collection(CHILD_LINKS_COLLECTION)
                .document(childNoteId), mapOf("id" to childNoteId)
        )
    }

    private suspend fun getNote(id: String) : Note {
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .document(id)
            .get()
            .await()
            ?.toNote(noteTypes)
            ?: throw Exception("Note not found")
    }

    private suspend fun getNoteByCaption(caption: String) : Note? {
        val result = firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .whereEqualTo("caption", caption)
            .get()
            .await()
            .documents
        return if (result.isEmpty()) null else result[0]
            ?.toNote(noteTypes)
            ?: throw Exception("Note not found")
    }

    private suspend fun getParentLinks(id: String) : List<DictionaryElement> {
        val ids = getParentIds(id)
        if (ids.isEmpty()) {
            return listOf()
        }
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .whereIn("id", ids)
            .get()
            .await()
            ?.map {doc -> doc.toDictionaryElement(noteTypes)}
            ?: emptyList()
    }

    private suspend fun getParentIds(noteId: String) : List<String> {
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .document(noteId)
            .collection(PARENT_LINKS_COLLECTION)
            .get()
            .await()
            ?.map { it.id }
            ?: emptyList()
    }

    private suspend fun getChildrenIds(noteId: String) : List<String> {
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .document(noteId)
            .collection(CHILD_LINKS_COLLECTION)
            .get()
            .await()
            ?.map { it.id }
            ?: emptyList()
    }
}

private fun Note.toMap() : Map<String, Any> {
    return mapOf(
        "id" to  id,
        "type" to type.id,
        "caption" to caption,
        "description" to description,
        "color" to color.value.toLong(),
        "start" to start.toInstant().epochSecond,
        "finish" to finish.toInstant().epochSecond,
        "root" to root,
        "textInsertions" to textInsertions.values.asSequence().map {e -> Pair(e.id, e.caption) }.toMap()
    )
}

private fun DocumentSnapshot.toNote(types: Map<String, NoteType>) : Note {
    return Note(
        id = data?.get("id") as String,
        caption = data?.get("caption") as String,
        type = types[data?.get("type") as String]!!,
        description = data?.get("description") as String,
        color = Color(data?.get("color") as Long),
        start = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data?.get("start") as Long), ZoneId.systemDefault()),
        finish = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data?.get("finish") as Long), ZoneId.systemDefault()),
        root = (data?.get("root") ?: false) as Boolean,
        textInsertions = buildTextInsertions(data?.get("textInsertions"))
    )
}
private fun buildTextInsertions(data: Any?): Map<String, DictionaryElement> {
    if (data == null) return emptyMap()
    return (data as Map<String, String>).entries.asSequence()
        .map { e -> Pair(e.key, DictionaryElement(id = e.key, type = NoteType(), caption = e.value)) }
        .toMap()
}