@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.TimeDefault
import ru.vat78.notes.clients.android.data.User

class NoteTypeRepository (
    private val user: User,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
): NoteTypeStorage {

    override suspend fun getTypes(): Collection<NoteType> {
        return withContext(Dispatchers.IO) {
            val data: List<NoteType> = firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTE_TYPES_COLLECTION)
                .get()
                .await()
                .map {doc -> doc.toNoteType()}
            Log.i("Firestore. Note types repository", "Note types are loaded: $data")
            return@withContext data.toSet()
        }
    }

    override suspend fun save(type: NoteType){
        val collection = firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTE_TYPES_COLLECTION)
        collection.document(type.id).set(type.toDocument())
        Log.i("Firestore. Note types repository", "Type $type was saved")
    }

}

private fun DocumentSnapshot.toNoteType() : NoteType {
    return NoteType(
        id = data?.get("id") as String,
        name = data?.get("name") as String,
        icon = data?.get("icon") as String,
        tag = data?.get("tag") as Boolean,
        hierarchical = data?.get("hierarchical") as Boolean,
        symbol = (data?.get("symbol") as String).first(),
        defaultStart = TimeDefault.valueOf(data?.get("defaultStart") as String),
        defaultFinish = TimeDefault.valueOf(data?.get("defaultFinish") as String),
        default = data?.getOrDefault("default", false) as Boolean,
    )
}

private fun NoteType.toDocument() : Map<String, Any> {
    return mapOf(
        "id" to id,
        "name" to name,
        "icon" to icon,
        "tag" to tag,
        "hierarchical" to hierarchical,
        "symbol" to symbol.toString(),
        "defaultStart" to defaultStart.name,
        "defaultFinish" to defaultFinish.name
    )
}