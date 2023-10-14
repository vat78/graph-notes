package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteSyncStorage
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypes
import ru.vat78.notes.clients.android.data.User
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class NoteRepository (
    private val user: User,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteSyncStorage {

    override suspend fun save(note: Note) {
        Log.i("Firestore.NoteRepository", "Update note ${note.id}, timestamp: ${note.lastUpdate}")
        if (note.id.isBlank()) {
            return
        }
        withContext(Dispatchers.IO) {
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .document(note.id)
                .set(note.toMap(), SetOptions.merge())
        }
    }

    override suspend fun getNotesForSync(from: Long, to: Long): List<Note> {
        return withContext(Dispatchers.IO) {
            val collection = firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
            val query = if (from == 0L) collection else collection
                .where(Filter.or(
                    Filter.equalTo("lastUpdate", null),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("lastUpdate", from),
                        Filter.lessThan("lastUpdate", to)
                    )
                ))
            val notes = query
                .get()
                .await()
                .map { doc -> doc.toNote() }
            val forTimeFix = notes.filter { it.lastUpdate == 0L }.map { it.copy(lastUpdate = to - 1) }.toList()
            Log.i("NoteRepository", "Found ${notes.size} notes for sync for time from $from to $to. And ${forTimeFix.size} of them need to be updated.")
            if (forTimeFix.isNotEmpty()) {
                firestore.runBatch { batch ->
                    forTimeFix.forEach {
                        batch.set(
                            firestore.collection(USER_COLLECTION)
                                .document(user.id)
                                .collection(NOTES_COLLECTION)
                                .document(it.id), it.toMap())
                    }
                }
            }
            return@withContext notes
        }
    }
}

private fun Note.toMap() : Map<String, Any?> {
    return mapOf(
        "id" to  id,
        "type" to type.id,
        "caption" to caption,
        "description" to description,
        "color" to color.value.toLong(),
        "start" to start.toInstant().epochSecond,
        "finish" to finish.toInstant().epochSecond,
        "root" to root,
        "textInsertions" to textInsertions.values.asSequence().map {e -> Pair(e.id, e.caption) }.toMap(),
        "lastUpdate" to lastUpdate,
        "deleted" to deleted
    )
}

private fun DocumentSnapshot.toNote() : Note {
    return Note(
        id = data?.get("id") as String,
        caption = data?.get("caption") as String,
        type = NoteTypes.getNoteTypeById(data?.get("type") as String),
        description = data?.get("description") as String,
        color = Color(data?.get("color") as Long),
        start = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data?.get("start") as Long), ZoneId.systemDefault()),
        finish = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data?.get("finish") as Long), ZoneId.systemDefault()),
        root = (data?.get("root") ?: false) as Boolean,
        textInsertions = buildTextInsertions(data?.get("textInsertions")),
        lastUpdate = data?.get("lastUpdate") as Long? ?: 0,
        deleted = data?.get("deleted") as Boolean? ?: false
    )
}

private fun buildTextInsertions(data: Any?): Map<String, DictionaryElement> {
    if (data == null) return emptyMap()
    return (data as Map<String, String>).entries.asSequence()
        .map { e -> Pair(e.key, DictionaryElement(id = e.key, type = NoteType(tag = true), caption = e.value)) }
        .toMap()
}