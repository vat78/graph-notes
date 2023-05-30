@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.defaultTypes

class NoteTypeRepository (
    val user: User,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                .snapshots()
                .mapLatest<QuerySnapshot, List<NoteType>> { snapshot -> snapshot.toObjects() }
                .first()
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