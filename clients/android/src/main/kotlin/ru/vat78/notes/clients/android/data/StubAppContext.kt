package ru.vat78.notes.clients.android.data

import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class StubAppContext : AppContext {
    private val notes: MutableList<Note> = arrayListOf(
        Note(type = defaultTypes.first { it.default }, caption = "test 1"),
        Note(type = defaultTypes.first { it.default }, caption = "Test 2"),
        Note(type = defaultTypes.first { it.default }, caption = "Test formatted", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 23, 18, 55), UTC), description = "shfsh ;ljrg sdfbgsn @test fl;cvbmvcx fdsdfblmkb sbksdlkb sbnlskdgb dsklbnlksdbn  sdfbldskblksd dsbkmdsblksm sdfkblmskldbm sdbmsldkbm  sdmbflkdsmbsl dsbmlkdsmb sdbklmsldkbms sdmflbsld"),
        Note(type = defaultTypes.first { it.default }, id = "test-uuid", caption = "test 3", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 22, 18, 55), UTC), description = "dlfak *vblmafbvmf afbmafdbma* abfdbbadabd"),
        Note(type = defaultTypes.first { it.default }, caption = "test 4", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 22, 18, 0), UTC)),
        Note(caption = "test tag", type = defaultTypes.first { !it.default }),
        Note(caption = "super tag", type = defaultTypes.first { !it.default }),
    )

    private val parentChild: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val childParent: MutableMap<String, MutableSet<String>> = mutableMapOf()

    lateinit var newNote: NoteWithParents

    override val user: User
        get() = User("0", "ANONIMUS", email = "anonimus@test.org")
    override val userStorage: UserStorage
        get() = object: UserStorage {
            override suspend fun saveUser(user: User?) {
            }
        }

    override val noteTypeStorage: NoteTypeStorage
        get() = object: NoteTypeStorage {
            private val _cache = defaultTypes.associateBy { it.id }
            override val types: Map<String, NoteType>
                get() = _cache

            override suspend fun reload() {}

            override suspend fun getDefaultType(): NoteType {
                return _cache.values.first { it.default }
            }
        }
    override val noteStorage: NoteStorage
        get() = object: NoteStorage {
            override suspend fun getNotes(filter: NotesFilter): List<Note> {
                return notes.filter {
                               filter.typesToLoad?.contains(it.type.id) ?: true
                            && filter.noteIdsForLoad?.contains(it.id) ?: true
                            && !(filter.typesToExclude?.contains(it.type.id) ?: false)
                }.sortedBy { it.start }
            }

            override fun buildNewNote(type: NoteType, text: String, parent: Note?, insertions: Set<DictionaryElement>) {
                val startTime = generateTime(type.defaultStart, ZonedDateTime::now)
                val finishTime = generateTime(type.defaultFinish, ZonedDateTime::now)
                val note: Note by lazy {
                    if (type.tag) {
                        Note(
                            type = type,
                            caption = text,
                            start = startTime,
                            finish = finishTime,
                        )
                    } else {
                        Note(
                            type = type,
                            description = text,
                            start = startTime,
                            finish = finishTime,
                        )
                    }
                }
                newNote = if (parent == null) {
                    NoteWithParents(note, emptySet())
                } else {
                    NoteWithParents(note, setOf(DictionaryElement(parent)))
                }
            }

            override suspend fun getNoteWithParents(uuid: String): NoteWithParents {
                TODO("Not yet implemented")
            }

            override suspend fun getNoteWithChildren(uuid: String): NoteWithChildren {
                TODO("Not yet implemented")
            }

            override suspend fun saveNote(note: Note, parents: Set<DictionaryElement>): Note {
                TODO("Not yet implemented")
            }

            override suspend fun getTags(filter: NotesFilter): List<DictionaryElement> {
                TODO("Not yet implemented")
            }

            override suspend fun updateNote(note: Note) {
                TODO("Not yet implemented")
            }
        }

    override val tagSearchService: TagSearchService
        get() = object: TagSearchService() {
            override suspend fun searchTagSuggestions(
                words: Set<String>,
                excludedTypes: List<String>,
                excludedTags: Set<String>
            ): List<DictionaryElement> {
                TODO("Not yet implemented")
            }

            override suspend fun deleteTagSuggestions(tokens: Set<String>, tagId: String) {
                TODO("Not yet implemented")
            }

            override suspend fun updateTagSuggestions(tokens: Set<String>, tagId: String, typeId: String) {
                TODO("Not yet implemented")
            }
        }

    fun loadNotes(type: String?) : List<Note> {
        if (type == null) {
            return notes.toList()
        }
        return notes
            .filter { note -> note.type.id == type }
    }

    fun getOneNote(uuid: String): Note {
        if (uuid == "new") {
            return newNote.note
        }
        return notes
            .asSequence()
            .filter { note -> note.id == uuid }
            .first()
    }

    fun getNoteTypeById(id: String): NoteType {
        return defaultTypes.find { type -> type.id == id }!!
    }

    fun saveNote(note: Note) {
        val existingNote = notes.find { n -> n.id == note.id }
        if (existingNote != null) {
            notes.remove(existingNote)
        }
        notes.add(note)
        notes.sortBy { n -> n.start }
    }

    fun getAllByName(name: String, typeFilter: Set<String>, timeFilter: Boolean): List<DictionaryElement> {
        if (name.length < 3) {
            return listOf()
        }
        return notes
            .asSequence()
            .filter { note -> (typeFilter.isEmpty() || typeFilter.contains(note.type.id)) }
            .filter { note -> !timeFilter || (note.start.isAfter(ZonedDateTime.now()) && note.finish.isBefore(ZonedDateTime.now())) }
            .filter { note -> note.caption.contains(name, ignoreCase = true) }
            .map { note -> DictionaryElement(note) }
            .take(7)
            .toList()
    }

    fun getClosestTimeOrDefault(default: ZonedDateTime): ZonedDateTime {
        val midnight = default.truncatedTo(ChronoUnit.DAYS)
        return notes
            .asSequence()
            .filter { note -> note.type == defaultTypes[0] && note.finish.isAfter(midnight) }
            .maxOfOrNull { note -> note.finish } ?: default
    }

    fun getParent(uuid: String): DictionaryElement? {
        return childParent[uuid]?.firstOrNull()?.let { DictionaryElement(getOneNote(it)) }
    }

    fun getLinks(uuid: String): Set<DictionaryElement> {
        return childParent[uuid]?.map { DictionaryElement(getOneNote(it)) }?.toSet() ?: setOf()
    }

    fun saveLinks(root: String, links: Set<DictionaryElement>) {
        childParent[root] = links.map { it.id }.toMutableSet()
        links.forEach { link ->
            (parentChild[link.id] ?: mutableSetOf()).add(root)
        }
    }

    fun addLink(parent: String, child: String) {
        (parentChild[parent] ?: mutableSetOf()).add(child)
        (childParent[child] ?: mutableSetOf()).add(parent)
    }
}