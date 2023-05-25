package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.ObjectType
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.defaultTypes

class NoteTypeRepository (
    val user: User,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteTypeStorage {

    private var _cache: Map<String, ObjectType> = emptyMap()
    override val types
        get() = _cache

    override suspend fun reload() {
        val data: List<ObjectType> = firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTE_TYPES_COLLECTION)
            .snapshots()
            .map<QuerySnapshot, List<ObjectType>> { snapshot -> snapshot.toObjects() }
            .first()
        if (data.isEmpty()) {
            saveToCache(storeDefaultTypes())
        } else {
            saveToCache(data)
        }
        Log.i("Note types repository", "Note types reloaded and ${_cache.size} values were cached")
    }

    override suspend fun getDefaultType(): ObjectType {
        return types.values.first { it.default }
    }

    private fun storeDefaultTypes(): List<ObjectType> {
        val collection = firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTE_TYPES_COLLECTION)
        defaultTypes.forEach { type ->
            collection.document(type.id).set(type)
        }
        Log.i("Note types repository", "Default values were created")
        return defaultTypes
    }

    private fun saveToCache(types: List<ObjectType>) {
        _cache = types.associateBy { it.id }
    }
}