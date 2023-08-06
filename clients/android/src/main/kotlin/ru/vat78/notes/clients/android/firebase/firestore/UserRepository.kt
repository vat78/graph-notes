package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.UserStorage

class UserRepository(
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserStorage {

    override suspend fun saveUser(newUser: User?) {
        withContext(Dispatchers.IO) {
            if (newUser != null) {
                firestore.collection(USER_COLLECTION).document(newUser.id).set(newUser, SetOptions.merge())
                Log.i("User repository", "User ${newUser.name} saved to database")
            }
        }
    }

    override suspend fun getLastSyncTimestamp(userId: String, deviceId: String): Long {
        return withContext(Dispatchers.IO) {
            val data = firestore.collection(USER_COLLECTION)
                .document(userId)
                .collection(SYNC_HISTORY_COLLECTION)
                .whereEqualTo("deviceId", deviceId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .map {doc -> doc?.get("timestamp") as Long}
            if (data.isEmpty()) {
                return@withContext 0L
            } else {
                data[0]
            }
        }
    }

    override suspend fun saveLastSyncTimestamp(userId: String, deviceId: String, timestamp: Long) {
        withContext(Dispatchers.IO) {
            firestore.collection(USER_COLLECTION)
                .document(userId)
                .collection(SYNC_HISTORY_COLLECTION)
                .document(deviceId + timestamp)
                .set(mapOf("deviceId" to deviceId, "timestamp" to timestamp))
        }
    }

}