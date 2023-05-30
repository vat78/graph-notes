@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.vat78.notes.clients.android.firebase.firestore

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.TagSearchService
import ru.vat78.notes.clients.android.data.User

class TagSearchRepository(
    val user: User,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TagSearchService() {

    override suspend fun searchTagSuggestions(
        words: Set<String>,
        excludedTypes: List<String>,
        excludedTags: Set<String>
    ): List<DictionaryElement> {
        val result = withContext(Dispatchers.IO) {
            val ids = words.asIterable()
                .map { findIdsByWord(word = it, excluded = excludedTypes) }
                .map { it.first() }
                .fold(emptySet<String>()){ acc, list ->
                    if (acc.isEmpty() || list.isEmpty()) acc + list else acc.intersect(list)
                }
                .subtract(excludedTags)

            return@withContext if (ids.isEmpty()) emptyList<DictionaryElement>()
            else firestore.collection(USER_COLLECTION)
                .document(user.id)
                .collection(NOTES_COLLECTION)
                .whereIn("id", ids.toList())
                .get()
                .await()
                .map {doc -> doc.toDictionaryElement()}
        }
        return result
    }

    override suspend fun deleteTagSuggestions(tokens: Set<String>, tagId: String) {
        withContext(Dispatchers.IO) {
            tokens.forEach{word ->
                firestore.collection(USER_COLLECTION)
                    .document(user.id)
                    .collection(SUGGESTION_COLLECTION)
                    .document(word)
                    .collection(CHILD_LINKS_COLLECTION)
                    .document(tagId)
                    .delete()
            }
        }
    }

    override suspend fun updateTagSuggestions(tokens: Set<String>, tagId: String, typeId: String) {
        withContext(Dispatchers.IO) {
            tokens.forEach { word ->
                firestore.collection(USER_COLLECTION)
                    .document(user.id)
                    .collection(SUGGESTION_COLLECTION)
                    .document(word)
                    .collection(CHILD_LINKS_COLLECTION)
                    .document(tagId)
                    .set(mapOf("tag" to tagId, "type" to typeId))
            }
        }
    }

    private fun findIdsByWord(word: String, type: String? = null, excluded: List<String> = emptyList()): Flow<Set<String>> {
        if (word.length < 2) {
            return emptyFlow()
        }
        val query = firestore.collection(USER_COLLECTION)
            .document(user.id)
            .collection(SUGGESTION_COLLECTION)
            .document(word)
            .collection(CHILD_LINKS_COLLECTION)
        val queryWithType = if (type != null) {
            query.whereEqualTo("type", type)
        } else if (excluded.isNotEmpty())  {
            query.whereNotIn("type", excluded)
        } else {
            query
        }
        return queryWithType
            .orderBy("type")
            .limit(5)
            .snapshots()
            .mapLatest { s -> s.documents.asSequence()
                .map { doc -> doc.get("tag") as String?}
                .filter { true }
                .map { v -> v as String }
                .toSet() }
    }
}

fun DocumentSnapshot.toDictionaryElement() : DictionaryElement {
    return DictionaryElement(
        id = data?.get("id") as String,
        caption = data?.get("caption") as String,
        type = data?.get("type") as String,
        color = Color(data?.get("color") as Long)
    )
}