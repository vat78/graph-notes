package ru.vat78.notes.clients.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.UserStorage

class UserRepository(
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserStorage {

    override suspend fun saveUser(user: User?) {
        if (user == null) return
        firestore.collection(USER_COLLECTION).document(user.id).set(user)
        Log.i("User repository", "User ${user.name} saved to database")
    }

}