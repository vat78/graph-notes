package ru.vat78.notes.clients.android.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import ru.vat78.notes.clients.android.data.ExternalStorage
import ru.vat78.notes.clients.android.data.NoteLinkSyncStorage
import ru.vat78.notes.clients.android.data.NoteSyncStorage
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.UserStorage

class FirestoreContext(
    override val user: User
) : ExternalStorage {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override val userStorage: UserStorage = UserRepository(firestore = firestore)
    override val noteTypeStorage: NoteTypeStorage = NoteTypeRepository(user = user, firestore = firestore)
    override val noteStorage: NoteSyncStorage = NoteRepository(user = user, firestore = firestore)
    override val linkStorage: NoteLinkSyncStorage = NoteLinkRepository(user = user, firestore = firestore)
}