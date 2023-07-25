package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
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

}