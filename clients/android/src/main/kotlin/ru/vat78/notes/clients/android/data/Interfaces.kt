package ru.vat78.notes.clients.android.data

import kotlin.streams.toList

interface UserStorage {
    suspend fun saveUser(user: User?)
}

interface NoteTypeStorage {
    val types : Map<String, NoteType>
    suspend fun reload()
    suspend fun getDefaultType(): NoteType
}

interface NoteStorage {
    suspend fun getNotes(types: List<String> = emptyList()): List<Note>
    fun buildNewNote(type: NoteType, text: String, parent: Note? = null)
    suspend fun getNoteForEdit(uuid: String): NoteWithLinks
    suspend fun saveNote(note: Note, parents: Set<DictionaryElement>)
    suspend fun insertChild(child: Note, parents: Set<DictionaryElement>)
}

abstract class TagSearchService {
    abstract suspend fun searchTagSuggestions(
        text: String,
        excludedTypes: List<String>,
        excludedTags: List<String>): List<DictionaryElement>

    suspend fun searchTagSuggestions(text: String, existingLinks: Set<DictionaryElement>, typeInfoSource: (String) -> NoteType): List<DictionaryElement> {
        val excludedTags = existingLinks.map { it.id }
        val excludedTypes = existingLinks.stream()
            .map { it.type }
            .distinct()
            .map { typeInfoSource(it) }
            .filter(NoteType::hierarchical)
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