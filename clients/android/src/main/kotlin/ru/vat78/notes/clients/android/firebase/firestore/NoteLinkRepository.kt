package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteLink
import ru.vat78.notes.clients.android.data.NoteLinkSyncStorage
import ru.vat78.notes.clients.android.data.NoteSyncStorage
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.User
import java.time.Instant

class NoteLinkRepository (
    private val user: User,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteLinkSyncStorage {

    override suspend fun saveLinks(links: Collection<NoteLink>) {
        withContext(Dispatchers.IO) {
            firestore.runBatch { batch ->
                links.forEach {
                    insertLink(batch, it.childId, it.parentId, it.deleted, it.lastUpdate)
                }
            }
        }
    }

    override suspend fun getLinksForSync(from: Long, to: Long): List<NoteLink> {
        return withContext(Dispatchers.IO) {
//            val links = firestore.collection(USER_COLLECTION)
//                .document(user.id)
//                .collection(NOTES_COLLECTION)
//                .get()
//                .await()
//                .map { doc -> doc.id }
//                .flatMap { id -> getParentIds(id).map { parent -> NoteLink(parentId = parent, childId = id, deleted = false) } }
//

            val links = firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(LINK_COLLECTION)
                .where(Filter.or(
                    Filter.equalTo("lastUpdate", null),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("lastUpdate", from),
                        Filter.lessThan("lastUpdate", to)
                    )
                ))
                .get()
                .await()
                .map { doc -> NoteLink(
                    parentId = doc?.get("parentId") as String,
                    childId = doc.get("childId") as String,
                    deleted = doc.get("deleted") as Boolean? ?: false,
                    lastUpdate = doc.get("lastUpdate") as Long? ?: 0,
                ) }
            val forTimeFix = links.filter { it.lastUpdate == 0L }.toList()
            if (forTimeFix.isNotEmpty()) {
                firestore.runBatch { batch ->
                    forTimeFix.forEach {
                        updateLink(batch, it.childId, it.parentId, it.deleted, to - 1)
                    }
                }
            }

            return@withContext links
        }
    }

    private fun insertLink(batch: WriteBatch, childNoteId: String, parentNoteId: String, deleted: Boolean, timestamp: Long = Instant.now().epochSecond) {
//        batch.set(
//            firestore.collection(USER_COLLECTION)
//                .document(user.id)
//                .collection(NOTES_COLLECTION)
//                .document(childNoteId)
//                .collection(PARENT_LINKS_COLLECTION)
//                .document(parentNoteId), mapOf("id" to parentNoteId)
//        )
//        batch.set(
//            firestore.collection(USER_COLLECTION)
//                .document(user.id)
//                .collection(NOTES_COLLECTION)
//                .document(parentNoteId)
//                .collection(CHILD_LINKS_COLLECTION)
//                .document(childNoteId), mapOf("id" to childNoteId)
//        )
        updateLink(batch, childNoteId, parentNoteId, deleted, timestamp)
    }

    private fun updateLink(batch: WriteBatch, childNoteId: String, parentNoteId: String, deleted: Boolean, timestamp: Long) {
        batch.set(
            firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(LINK_COLLECTION)
                .document(parentNoteId + childNoteId), mapOf("parentId" to parentNoteId, "childId" to childNoteId, "lastUpdate" to timestamp, "deleted" to deleted)
        )
    }
}