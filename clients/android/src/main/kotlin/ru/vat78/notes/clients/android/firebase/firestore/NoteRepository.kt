package ru.vat78.notes.clients.android.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteStorage
import ru.vat78.notes.clients.android.data.NoteWithLinks
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.generateTime
import java.time.LocalDateTime

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
                .get()
                .await()
                .toObjects(Note::class.java)
        }
        return result
    }

    override fun buildNewNote(type: NoteType, text: String, parent: Note?) {
        val startTime = generateTime(type.defaultStart, LocalDateTime::now)
        val finishTime = generateTime(type.defaultFinish, LocalDateTime::now)
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

    private fun getNote(id: String) : Note {
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .document(id)
            .get()
            .result
            ?.toObject(Note::class.java)
            ?: throw Exception("Note not found")
    }

    private fun getParentLinks(id: String) : List<DictionaryElement> {
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .document(id)
            .collection(PARENT_LINKS_COLLECTION)
            .get()
            .result
            ?.toObjects(DictionaryElement::class.java)
            ?: emptyList()
    }
}

