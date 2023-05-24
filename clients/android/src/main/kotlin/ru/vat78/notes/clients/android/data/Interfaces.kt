package ru.vat78.notes.clients.android.data

import kotlin.streams.toList

interface UserStorage {
    suspend fun saveUser(user: User?)
}

interface NoteTypeStorage {
    val types : Map<String, ObjectType>
    suspend fun reload()
    suspend fun getDefaultType(): ObjectType
}

interface NoteStorage {
    suspend fun getNotes(types: List<String> = emptyList()): List<Note>
    fun buildNewNote(type: ObjectType, text: String, parent: Note? = null)
    suspend fun getNoteForEdit(uuid: String): NoteWithLinks
}

abstract class TagSearchService {
    abstract suspend fun searchTagSuggestions(
        text: String,
        excludedTypes: List<String>,
        excludedTags: List<String>): List<DictionaryElement>

    suspend fun searchTagSuggestions(text: String, existingLinks: Set<DictionaryElement>, typeInfoSource: (String) -> ObjectType): List<DictionaryElement> {
        val excludedTags = existingLinks.map { it.id }
        val excludedTypes = existingLinks.stream()
            .map { it.type }
            .distinct()
            .map { typeInfoSource(it) }
            .filter(ObjectType::hierarchical)
            .map { it.id }
            .toList()
        return searchTagSuggestions(text, excludedTypes, excludedTags)
    }
}

interface AppContext {
    val user: User
    val userStorage: UserStorage
    val noteTypeStorage: NoteTypeStorage
    val noteStorage: NoteStorage
    val tagSearchService: TagSearchService
}