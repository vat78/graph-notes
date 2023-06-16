package ru.vat78.notes.clients.android.data

interface UserStorage {
    suspend fun saveUser(user: User?)
}

interface NoteTypeStorage {
    val types : Map<String, NoteType>
    suspend fun reload()
    suspend fun getDefaultType(): NoteType
}

interface NoteStorage {
    suspend fun getNotes(filter: NotesFilter): List<Note>
    suspend fun getTags(filter: NotesFilter): List<DictionaryElement>
    fun buildNewNote(type: NoteType, text: String, parent: Note? = null)
    suspend fun getNoteWithParents(uuid: String): NoteWithParents
    suspend fun getNoteWithChildren(uuid: String): NoteWithChildren
    suspend fun saveNote(note: Note, parents: Set<DictionaryElement>)
    suspend fun updateNote(note: Note)
}

abstract class TagSearchService {
    abstract suspend fun searchTagSuggestions(
        words: Set<String>,
        excludedTypes: List<String>,
        excludedTags: Set<String>): List<DictionaryElement>

    // ToDo: add filtering by time of availability of tags
    // ToDo: add statistics of usage of suggestions and ordering by it
    suspend fun searchTagSuggestions(text: String, note: NoteWithParents): List<DictionaryElement> {
        val existingLinks = note.parents
        val excludedTags = existingLinks.map { it.id }.toSet() + note.note.id
        val excludedTypes = existingLinks.asSequence()
            .map { it.type }
            .distinct()
            .filter(NoteType::hierarchical)
            .map { it.id }
            .toList()

        val words = getWordsForSearch(text)
        return searchTagSuggestions(words, excludedTypes, excludedTags)
    }

    abstract suspend fun deleteTagSuggestions(tokens: Set<String>, tagId: String)
    abstract suspend fun updateTagSuggestions(tokens: Set<String>, tagId: String, typeId: String)

    suspend fun updateTagSuggestions(oldText: String, newText: String, tagId: String, typeId: String) {
        val oldTokens = buildSearchBlocks(oldText)
        val newTokens = buildSearchBlocks(newText)
        val forDeletion = oldTokens - newTokens
        if (forDeletion.isNotEmpty()) {
            deleteTagSuggestions(forDeletion, tagId)
        }
        if (newTokens.isNotEmpty()) {
            updateTagSuggestions(newTokens, tagId, typeId)
        }
    }
}

interface AppContext {
    val user: User
    val userStorage: UserStorage
    val noteTypeStorage: NoteTypeStorage
    val noteStorage: NoteStorage
    val tagSearchService: TagSearchService
}

data class NotesFilter (
    val noteIdsForLoad: List<String>? = null,
    val typesToLoad: List<String>? = null,
    val typesToExclude: List<String>? = null,
    val onlyRoots: Boolean = false
)