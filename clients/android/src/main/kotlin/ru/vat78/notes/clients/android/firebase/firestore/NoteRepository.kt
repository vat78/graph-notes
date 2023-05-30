package ru.vat78.notes.clients.android.firebase.firestore

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
import ru.vat78.notes.clients.android.data.NoteWithLinks
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.generateTime
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class NoteRepository (
    val user: User,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteStorage {

    private var newNote: NoteWithLinks? = null

    override suspend fun getNotes(types: List<String>): List<Note> {
        val result = withContext(Dispatchers.IO) {
            var query: Query = firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
            if (types.isNotEmpty()) {
                query = query.whereIn("type", types)
            }
            return@withContext query
                .orderBy("finish")
                .get()
                .await()
                .map {doc -> doc.toNote()}
        }
        return result
    }

    override fun buildNewNote(type: NoteType, text: String, parent: Note?) {
        val startTime = generateTime(type.defaultStart, ZonedDateTime::now)
        val finishTime = generateTime(type.defaultFinish, ZonedDateTime::now)
        val note: Note by lazy {
            if (type.tag) {
                Note(
                    type = type.id,
                    caption = text,
                    start = startTime,
                    finish = finishTime,
                )
            } else {
                Note(
                    type = type.id,
                    description = text,
                    start = startTime,
                    finish = finishTime,
                )
            }
        }
        newNote = if (parent == null) {
            NoteWithLinks(note, emptySet())
        } else {
            NoteWithLinks(note, setOf(DictionaryElement(parent)))
        }
    }

    override suspend fun getNoteForEdit(uuid: String): NoteWithLinks {
        if (uuid == "new") {
            return newNote ?: throw Exception("New note not created")
        }
        return NoteWithLinks(getNote(uuid), getParentLinks(uuid).toSet())
    }

    override suspend fun saveNote(note: Note, parents: Set<DictionaryElement>) {
        withContext(Dispatchers.IO) {
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
            ?.toNote()
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
            ?.map {doc -> doc.toDictionaryElement()}
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
}

private fun Note.toMap() : Map<String, Any> {
    return mapOf(
        "id" to  id,
        "type" to type,
        "caption" to caption,
        "description" to description,
        "color" to color.value.toLong(),
        "start" to start.toInstant().epochSecond,
        "finish" to finish.toInstant().epochSecond
    )
}

private fun DocumentSnapshot.toNote() : Note {
    return Note(
        id = data?.get("id") as String,
        caption = data?.get("caption") as String,
        type = data?.get("type") as String,
        description = data?.get("description") as String,
        color = Color(data?.get("color") as Long),
        start = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data?.get("start") as Long), ZoneId.systemDefault()),
        finish = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data?.get("finish") as Long), ZoneId.systemDefault())
    )
}
