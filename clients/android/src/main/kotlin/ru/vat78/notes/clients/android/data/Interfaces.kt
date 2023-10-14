package ru.vat78.notes.clients.android.data

import java.time.LocalDate
import java.time.ZonedDateTime

interface UserStorage {
    suspend fun saveUser(newUser: User?)
    suspend fun getLastSyncTimestamp(userId: String, deviceId: String): Long?
    suspend fun saveLastSyncTimestamp(userId: String, deviceId: String, timestamp: Long, stats: Map<String, Int>)
}

//abstract class TagSearchService {
//    abstract suspend fun searchTagSuggestions(
//        words: Set<String>,
//        excludedTypes: List<String>,
//        selectedType: String,
//        excludedTags: Set<String>,
//        maxCount: Int): List<DictionaryElement>
//
//    // ToDo: add filtering by time of availability of tags
//    // ToDo: add statistics of usage of suggestions and ordering by it
//    suspend fun searchTagSuggestions(text: String, note: NoteWithParents, maxCount: Int = 5): List<DictionaryElement> {
//        val existingLinks = note.parents
//        val excludedTags = existingLinks.map { it.id }.toSet() + note.note.id
//        val excludedTypes = existingLinks.asSequence()
//            .map { it.type }
//            .distinct()
//            .filter(NoteType::hierarchical)
//            .map { it.id }
//            .toList()
//
//        val words = getWordsForSearch(text)
//        return searchTagSuggestions(words, excludedTypes, "", excludedTags, maxCount)
//    }
//
//    abstract suspend fun deleteTagSuggestions(tokens: Set<String>, tagId: String)
//    abstract suspend fun updateTagSuggestions(tokens: Set<String>, tagId: String, typeId: String)
//
//    suspend fun updateTagSuggestions(oldText: String, newText: String, tagId: String, typeId: String) {
//        val oldTokens = buildSearchBlocks(oldText)
//        val newTokens = buildSearchBlocks(newText)
//        val forDeletion = oldTokens - newTokens
//        if (forDeletion.isNotEmpty()) {
//            deleteTagSuggestions(forDeletion, tagId)
//        }
//        if (newTokens.isNotEmpty()) {
//            updateTagSuggestions(newTokens, tagId, typeId)
//        }
//    }
//}


interface ExternalStorage {
    val user: User
    val userStorage: UserStorage
    val noteTypeStorage: NoteTypeStorage
    val noteStorage: NoteSyncStorage
    val linkStorage: NoteLinkSyncStorage
}

interface AppStorage {
    val user: User
    val userStorage: UserStorage
    val noteTypeStorage: NoteTypeStorage
    val noteStorage: NoteStorage
//    val tagSearchService: TagSearchService
    val tagStorage: TagStorage
    val suggestionStorage: WordStorage
    val linkStorage: NoteLinkStorage
}

data class NotesFilter (
    val noteIdsForLoad: Collection<String>? = null,
    val typesToLoad: List<String>? = null,
    val typesToExclude: List<String>? = null,
    val onlyRoots: Boolean = false,
    val count: Int = 1000,
    val before: ZonedDateTime = maxTime(),
    val after: ZonedDateTime = minTime()
)