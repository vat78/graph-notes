package ru.vat78.notes.clients.android.data

import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class StubAppContext : AppStorage {
    val notes: MutableList<Note> = arrayListOf(
        Note(type = NoteTypes.types.values.first { it.default }, caption = "test 1"),
        Note(type = NoteTypes.types.values.first { it.default }, caption = "Test 2"),
        Note(type = NoteTypes.types.values.first { it.default }, caption = "Test formatted", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 23, 18, 55), UTC), description = "shfsh ;ljrg sdfbgsn @test fl;cvbmvcx fdsdfblmkb sbksdlkb sbnlskdgb dsklbnlksdbn  sdfbldskblksd dsbkmdsblksm sdfkblmskldbm sdbmsldkbm  sdmbflkdsmbsl dsbmlkdsmb sdbklmsldkbms sdmflbsld"),
        Note(type = NoteTypes.types.values.first { it.default }, id = "test-uuid", caption = "test 3", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 22, 18, 55), UTC), description = "dlfak *vblmafbvmf afbmafdbma* abfdbbadabd"),
        Note(type = NoteTypes.types.values.first { it.default }, caption = "test 4", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 22, 18, 0), UTC)),
        Note(caption = "test tag", type = NoteTypes.types.values.first { !it.default }),
        Note(caption = "super tag", type = NoteTypes.types.values.first { !it.default }),
    )

    private val parentChild: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val childParent: MutableMap<String, MutableSet<String>> = mutableMapOf()

    lateinit var newNote: NoteWithParents

    override val user: User
        get() = User("0", "ANONIMUS", email = "anonimus@test.org")
    override val userStorage: UserStorage
        get() = object: UserStorage {
            override suspend fun saveUser(newUser: User?) {
            }
            override suspend fun getLastSyncTimestamp(userId: String, deviceId: String): Long {
                TODO("Not yet implemented")
            }
            override suspend fun saveLastSyncTimestamp(userId: String, deviceId: String, timestamp: Long, stats: Map<String, Int>) {
                TODO("Not yet implemented")
            }
        }

    override val noteTypeStorage: NoteTypeStorage
        get() = object: NoteTypeStorage {
            override suspend fun getTypes(): Collection<NoteType> {
                return notes.map { it.type }.distinct()
            }

            override suspend fun save(type: NoteType) {
                TODO("Not yet implemented")
            }
        }
    override val noteStorage: NoteStorage
        get() = object: NoteStorage {
            override suspend fun getById(id: String): Note? {
                return notes.firstOrNull { it.id == id }
            }

            override suspend fun save(note: Note) {
                val old = getById(note.id)
                if (old != null) { notes.remove(old) }
                notes.add(note)
            }

            override suspend fun updateSuggestions(notes: Collection<Note>) {
                TODO("Not yet implemented")
            }

            override suspend fun getNotesByTime(types: List<String>, from: ZonedDateTime, count: Int): List<Note> {
                return notes
            }

            override suspend fun getNotesByIdAdTime(
                ids: Collection<String>,
                types: List<String>,
                from: ZonedDateTime,
                count: Int
            ): List<Note> {
                return notes.filter {ids.contains(it.id) && types.contains(it.type.id) }
            }

            override suspend fun getNotesByType(types: List<String>, count: Int): List<Note> {
                return notes.filter { types.contains(it.type.id) }
            }

            override suspend fun getNotesForSync(from: Long, to: Long): List<Note> {
                TODO("Not yet implemented")
            }
        }

    override val tagStorage: TagStorage
        get() = object: TagStorage {
            override suspend fun getTagsByIds(ids: Collection<String>): Set<DictionaryElement> {
                return notes.filter { ids.contains(it.id) }.map { DictionaryElement(it) }.toSet()
            }

            override suspend fun getParentsByNote(noteId: String): Set<DictionaryElement> {
                val parents = childParent.get(noteId)
                if (parents.isNullOrEmpty()) {
                    return emptySet()
                }
                return getTagsByIds(parents)
            }

            override suspend fun findTagByCaption(caption: String): DictionaryElement? {
                return notes.filter { it.type.tag }.map { DictionaryElement(it) }.firstOrNull { it.caption == caption }
            }
        }

    override val suggestionStorage: WordStorage
        get() = object: WordStorage {
            override suspend fun findTagIdsByWords(words: Collection<String>): List<String> {
                TODO("Not yet implemented")
            }

            override suspend fun findTagIdsByWordsAndType(words: Collection<String>, tagType: String): Set<String> {
                TODO("Not yet implemented")
            }

            override suspend fun saveOrUpdate(words: Collection<String>): List<Long> {
                TODO("Not yet implemented")
            }

            override suspend fun insertForTag(wordIds: Collection<Long>, noteId: String, typeId: String) {
                TODO("Not yet implemented")
            }

            override suspend fun deleteAllByTagId(tagId: String) {
                TODO("Not yet implemented")
            }
        }

    override val linkStorage: NoteLinkStorage
        get() = object : NoteLinkStorage {
            override suspend fun getParentLinksByNoteId(noteId: String): Set<NoteLink> {
                val parents = childParent.get(noteId)
                if (parents.isNullOrEmpty()) {
                    return emptySet()
                }
                return parents.map { NoteLink( parentId = it, childId = noteId, deleted = false) }.toSet()
            }

            override suspend fun getChildrenIds(ids: Collection<String>, typeId: Collection<String>): Set<String> {
                val children = ids.flatMap { childParent.getOrDefault(it, emptySet()) }
                if (children.isNullOrEmpty()) {
                    return emptySet()
                }
                return children.toSet()
            }

            override suspend fun saveLinks(links: Collection<NoteLink>) {
                links.forEach{
                    val children = parentChild.getOrPut(it.parentId, { mutableSetOf() })
                    children.add(it.childId)

                    val parents = childParent.getOrPut(it.childId, { mutableSetOf() })
                    parents.add(it.parentId)
                }
            }

            override suspend fun getLinksForSync(from: Long, to: Long): List<NoteLink> {
                TODO("Not yet implemented")
            }
        }
}