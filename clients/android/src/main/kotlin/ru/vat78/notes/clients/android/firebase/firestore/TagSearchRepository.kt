package ru.vat78.notes.clients.android.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.last
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.TagSearchService
import ru.vat78.notes.clients.android.data.User

class TagSearchRepository(
    val user: User,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TagSearchService() {

    override suspend fun searchTagSuggestions(
        text: String,
        excludedTypes: List<String>,
        excludedTags: List<String>
    ): List<DictionaryElement> {
        return firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(NOTES_COLLECTION)
            .whereNotIn("type", excludedTypes)
            .whereNotIn("id", excludedTags)
            .snapshots()
            .last().toObjects(DictionaryElement::class.java)
    }
}