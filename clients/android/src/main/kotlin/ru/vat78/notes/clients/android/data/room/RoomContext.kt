package ru.vat78.notes.clients.android.data.room

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.AppStorage
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteLink
import ru.vat78.notes.clients.android.data.NoteStorage
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypeStorage
import ru.vat78.notes.clients.android.data.NoteWithChildren
import ru.vat78.notes.clients.android.data.NoteWithParents
import ru.vat78.notes.clients.android.data.NotesFilter
import ru.vat78.notes.clients.android.data.TagSearchService
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.UserStorage
import ru.vat78.notes.clients.android.data.buildSearchBlocks
import ru.vat78.notes.clients.android.data.generateTime
import ru.vat78.notes.clients.android.data.room.entity.LinkEntity
import ru.vat78.notes.clients.android.data.room.entity.NoteEntity
import ru.vat78.notes.clients.android.data.room.entity.NoteTypeEntity
import ru.vat78.notes.clients.android.data.room.entity.SuggestionEntity
import ru.vat78.notes.clients.android.data.room.entity.UserEntity
import ru.vat78.notes.clients.android.data.room.entity.WordEntity
import java.time.ZonedDateTime
import java.util.UUID

class RoomContext(): AppStorage {

    private var DB_INSTANCE: NoteRoomDatabase? = null

    fun init(context: Context) {
        synchronized(this) {
            var instance = DB_INSTANCE

            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteRoomDatabase::class.java,
                    "notes_database"
                ).fallbackToDestructiveMigration()
                    .build()

                DB_INSTANCE = instance
            }
        }
    }

    private var _user: User? = null
    override val user: User
        get() = _user ?: ANONYMOUS

    override val userStorage = UserRepository()
    override val noteTypeStorage = NoteTypeRepository()
    override val noteStorage = NoteRepository()
    override val tagSearchService = TagSearchRepository()

    suspend fun syncTypes(types: Collection<NoteType>) {
        withContext(Dispatchers.IO) {
            types.map { NoteTypeEntity(it) }.forEach { DB_INSTANCE!!.noteTypeDao().save(it) }
            noteTypeStorage.reload()
        }
    }
    
    companion object {
        val ANONYMOUS = User("0", "ANONYMOUS", email = "anonymous@test.org")
    }

    inner class UserRepository: UserStorage {
        override suspend fun getLastSyncTimestamp(userId: String, deviceId: String): Long {
            TODO("Not yet implemented")
        }

        override suspend fun saveLastSyncTimestamp(userId: String, deviceId: String, timestamp: Long) {
            TODO("Not yet implemented")
        }

        suspend fun getCurrentUser(): UserEntity? {
            val existingUsers = DB_INSTANCE!!.userDao().getAll()
            Log.i("RoomUserRepository", "Users in local DB: $existingUsers")
            return if (existingUsers.isEmpty()) null else existingUsers[0]
        }

        override suspend fun saveUser(newUser: User?) {
            withContext(Dispatchers.IO) {
                val oldUser = getCurrentUser()
                _user =
                    if (oldUser == null || oldUser.id == "0") null else User(oldUser.id, oldUser.name, oldUser.email)
                if (newUser == _user) return@withContext
                if (newUser?.id != _user?.id) {
                    Log.i("RoomUserRepository", "Cleanup DB")
                    DB_INSTANCE!!.linkDao().cleanup()
                    DB_INSTANCE!!.noteDao().cleanup()
                    DB_INSTANCE!!.noteTypeDao().cleanup()
                    DB_INSTANCE!!.userDao().cleanup()
                    DB_INSTANCE!!.suggestionDao().cleanup()
                    DB_INSTANCE!!.wordDao().cleanup()
                }
                _user = newUser
                val deviceId = oldUser?.deviceId ?: UUID.randomUUID().toString()
                DB_INSTANCE!!.userDao().save(UserEntity(user.id, user.name, user.email, deviceId))
            }
        }
    }

    inner class NoteTypeRepository: NoteTypeStorage {
        private var _typeById: Map<String, NoteType> = emptyMap()
        override val types: Map<String, NoteType>
            get() = _typeById

        override suspend fun reload() {
            Log.i("RoomNoteRepository", "Reload note types")
            withContext(Dispatchers.IO) {
                _typeById = DB_INSTANCE!!.noteTypeDao()
                    .getAll()
                    .map { it.toNoteType() }
                    .associateBy { it.id }
            }
        }

        override suspend fun getDefaultType(): NoteType {
            return _typeById.values.first { it.default }
        }
    }

    inner class NoteRepository: NoteStorage {

        private var newNote: NoteWithParents? = null

        override suspend fun getNotes(filter: NotesFilter): List<Note> {
            Log.i("RoomNoteRepository", "Request for notes with filter $filter")
            val types = noteTypeStorage.types
            return getNoteEntities(filter).map { it.toNote(types) }
        }

        override suspend fun getTags(filter: NotesFilter): List<DictionaryElement> {
            Log.i("RoomNoteRepository", "Request for tags with filter $filter")
            val types = noteTypeStorage.types
            return getNoteEntities(filter).map { it.toDictionary(types) }
        }

        override fun buildNewNote(type: NoteType, text: String, parent: Note?, insertions: Set<DictionaryElement>) {
            val startTime = generateTime(type.defaultStart, ZonedDateTime::now)
            val finishTime = generateTime(type.defaultFinish, ZonedDateTime::now)
            val note: Note by lazy {
                if (type.tag) {
                    Note(
                        type = type,
                        caption = text.trim(),
                        start = startTime,
                        finish = finishTime,
                    )
                } else {
                    Note(
                        type = type,
                        description = text.trim(),
                        start = startTime,
                        finish = finishTime,
                        textInsertions = insertions.associateBy { it.id }
                    )
                }
            }
            newNote = if (parent == null) {
                NoteWithParents(note, insertions)
            } else {
                NoteWithParents(note, insertions + DictionaryElement(parent))
            }
        }

        override suspend fun getNoteWithParents(uuid: String): NoteWithParents {
            Log.i("RoomNoteRepository", "Get note $uuid with its parents")
            if (uuid == "new") {
                return newNote ?: throw Exception("New note not created")
            }
            return withContext(Dispatchers.IO) {
                val noteEntity = DB_INSTANCE!!.noteDao().findById(uuid)
                val parents = DB_INSTANCE!!.noteDao().findParents(uuid).map { it.toDictionary(noteTypeStorage.types) }.associateBy { it.id }
                val note = noteEntity[0].toNote(noteTypeStorage.types)
                NoteWithParents(note, parents.values.toSet())
            }
        }

        override suspend fun getNoteWithChildren(uuid: String): NoteWithChildren {
            Log.i("RoomNoteRepository", "Get note $uuid with its children")
            return withContext(Dispatchers.IO) {
                val noteEntity = DB_INSTANCE!!.noteDao().findById(uuid)
                val children = DB_INSTANCE!!.noteDao().findChildren(uuid).map { it.id }
                val note = noteEntity[0].toNote(noteTypeStorage.types)
                NoteWithChildren(note, children)
            }
        }

        override suspend fun saveNote(note: Note, parents: Set<DictionaryElement>): Note {
            Log.i("RoomNoteRepository", "Save note ${note.id} with its parent links")
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.noteDao().save(NoteEntity(note))
                val links = parents.map { LinkEntity(parent = it.id, child = note.id) }.toSet()
                val dbLinks = DB_INSTANCE!!.linkDao().getParents(note.id)
                val forDeletion = (dbLinks - links).map { LinkEntity( parent = it.parent, child = it.child, deleted = true) }.toTypedArray()
                val newLinks = (links - dbLinks).toTypedArray()
                DB_INSTANCE!!.linkDao().save(*forDeletion)
                DB_INSTANCE!!.linkDao().save(*newLinks)
            }
            return note
        }

        override suspend fun updateNote(note: Note) {
            Log.i("RoomNoteRepository", "Update note ${note.id}")
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.noteDao().save(NoteEntity(note))
            }
        }

        override suspend fun getNotesForSync(from: Long, to: Long): List<Note> {
            val safeFrom = if (from == 0L) 1 else from
            return withContext(Dispatchers.IO) {
                val entities = DB_INSTANCE!!.noteDao().getNotesForSync(safeFrom, to)
                if (entities.isNotEmpty()) {
                    val types = noteTypeStorage.types
                    return@withContext entities.map { it.toNote(types) }.toList()
                }
                return@withContext emptyList<Note>()
            }
        }

        override suspend fun getLinksForSync(from: Long, to: Long): List<NoteLink> {
            val safeFrom = if (from == 0L) 1 else from
            return withContext(Dispatchers.IO) {
                DB_INSTANCE!!.linkDao().getLinksForSync(safeFrom, to)
                    .map { NoteLink(it.parent, it.child, it.deleted, lastUpdate = 0) }
                    .toList()
            }
        }

        private suspend fun getNoteEntities(filter: NotesFilter): List<NoteEntity> {
            if (filter.noteIdsForLoad?.isEmpty() == true) {
                return emptyList()
            }
            return withContext(Dispatchers.IO) {
                val types =
                    if (filter.typesToLoad.isNullOrEmpty()) noteTypeStorage.types.keys else filter.typesToLoad

                if (filter.noteIdsForLoad.isNullOrEmpty()) {
                    if (filter.onlyRoots) {
                        DB_INSTANCE!!.noteDao().findOnlyRoots(types)
                    } else {
                        DB_INSTANCE!!.noteDao().findByTypes(types)
                    }
                } else {
                    if (filter.onlyRoots) {
                        DB_INSTANCE!!.noteDao().findByIdsOnlyRoots(types, filter.noteIdsForLoad)
                    } else {
                        DB_INSTANCE!!.noteDao().findByIds(types, filter.noteIdsForLoad)
                    }
                }
            }
        }

        suspend fun saveNotes(vararg notes:NoteEntity) {
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.noteDao().saveAll(*notes)
            }
        }

        suspend fun deleteNotes(vararg notes:NoteEntity) {
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.noteDao().delete(*notes)
            }
        }

        override suspend fun deleteLinks(links: List<NoteLink>) {
            val entities = links.map { LinkEntity(it) }.toTypedArray()
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.linkDao().delete(*entities)
            }
        }

        override suspend fun saveSyncingLinks(links: List<NoteLink>) {
            val entities = links.map { LinkEntity(it, cleanLastUpdate = true) }.toTypedArray()
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.linkDao().save(*entities)
            }
        }
    }

    inner class TagSearchRepository : TagSearchService() {
        override suspend fun searchTagSuggestions(
            words: Set<String>,
            excludedTypes: List<String>,
            excludedTags: Set<String>
        ): List<DictionaryElement> {
            return withContext(Dispatchers.IO) {
                val wordIds = DB_INSTANCE!!.wordDao().findWords(words).map { it.wordId }.toList()
                DB_INSTANCE!!.noteDao().findTagsForSuggestions(wordIds, excludedTypes, excludedTags)
                    .map { it.toDictionary(noteTypeStorage.types) }.toList()
            }
        }

        override suspend fun deleteTagSuggestions(tokens: Set<String>, tagId: String) {
            withContext(Dispatchers.IO) {
                val suggestions = DB_INSTANCE!!.wordDao().findWords(tokens).map { SuggestionEntity(it.wordId, tagId, "") }.toTypedArray()
                DB_INSTANCE!!.suggestionDao().delete(*suggestions)
            }
        }

        override suspend fun updateTagSuggestions(tokens: Set<String>, tagId: String, typeId: String) {
            withContext(Dispatchers.IO) {
                val words = DB_INSTANCE!!.wordDao().findWords(tokens)
                val newWords = (tokens - words.map { it.word }.toSet()).map { WordEntity(word = it) }.toTypedArray()
                DB_INSTANCE!!.wordDao().insert(*newWords)
                val suggestions = DB_INSTANCE!!.wordDao().findWords(tokens).map { SuggestionEntity(it.wordId, tagId, typeId) }.toTypedArray()
                DB_INSTANCE!!.suggestionDao().insert(*suggestions)
            }
        }

        suspend fun updateTagSuggestions(tag: Note) {
            withContext(Dispatchers.IO) {
                DB_INSTANCE!!.suggestionDao().deleteForTag(tag.id)
                val tokens = buildSearchBlocks(tag.caption)
                updateTagSuggestions(tokens, tag.id, tag.type.id)
            }
        }
    }
}

