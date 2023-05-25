package ru.vat78.notes.clients.android.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import ru.vat78.notes.clients.android.data.AppContext
import ru.vat78.notes.clients.android.data.NoteStorage
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.TagSearchService
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.UserStorage

class FirestoreContext(
    override val user: User
) : AppContext {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override val userStorage: UserStorage = UserRepository(firestore = firestore)
    override val noteTypeStorage: NoteTypeStorage = NoteTypeRepository(user = user, firestore = firestore)
    override val noteStorage: NoteStorage = NoteRepository(user = user, firestore = firestore)
    override val tagSearchService: TagSearchService = TagSearchRepository(user = user, firestore = firestore)
}