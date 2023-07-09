@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.TimeDefault
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.defaultTypes

class NoteTypeRepository (
    private val user: User,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteTypeStorage {

    private var _cache: Map<String, NoteType> = emptyMap()
    override val types
        get() = _cache

    override suspend fun reload() {
        Log.i("Note types repository", "Start reloading...")
        withContext(Dispatchers.IO) {
            val data: List<NoteType> = firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTE_TYPES_COLLECTION)
                .get()
                .await()
                .map {doc -> doc.toNoteType()}
            if (data.isEmpty()) {
                saveToCache(storeDefaultTypes())
            } else {
                saveToCache(data)
            }
            Log.i("Note types repository", "Note types reloaded and ${_cache.size} values were cached")
        }
    }

    override suspend fun getDefaultType(): NoteType {
        return types.values.first { it.default }
    }

    private fun storeDefaultTypes(): List<NoteType> {
        val collection = firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTE_TYPES_COLLECTION)
        defaultTypes.forEach { type ->
            collection.document(type.id).set(type)
        }
        Log.i("Note types repository", "Default values were created")
        return defaultTypes
    }

    private fun saveToCache(types: List<NoteType>) {
        _cache = types.associateBy { it.id }
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
        default = data?.get("default") as Boolean,
    )
}