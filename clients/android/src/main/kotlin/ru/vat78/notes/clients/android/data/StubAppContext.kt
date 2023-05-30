package ru.vat78.notes.clients.android.data

import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class StubAppContext : AppContext {
    private val notes: MutableList<Note> = arrayListOf(
        Note(type = defaultTypes.first { it.default }.id, caption = "test 1"),
        Note(type = defaultTypes.first { it.default }.id, caption = "test 2"),
        Note(type = defaultTypes.first { it.default }.id, caption = "test formatted", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 23, 18, 55), UTC), description = "shfsh ;ljrg sdfbgsn @test fl;cvbmvcx fdsdfblmkb sbksdlkb sbnlskdgb dsklbnlksdbn  sdfbldskblksd dsbkmdsblksm sdfkblmskldbm sdbmsldkbm  sdmbflkdsmbsl dsbmlkdsmb sdbklmsldkbms sdmflbsld"),
        Note(type = defaultTypes.first { it.default }.id, id = "test-uuid", caption = "test 3", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 22, 18, 55), UTC), description = "dlfak *vblmafbvmf afbmafdbma* abfdbbadabd"),
        Note(type = defaultTypes.first { it.default }.id, caption = "test 4", start = ZonedDateTime.of(LocalDateTime.of(2023, 3, 22, 18, 0), UTC)),
        Note(caption = "test tag", type = defaultTypes.first { !it.default }.id),
        Note(caption = "super tag", type = defaultTypes.first { !it.default }.id),
    )

    private val parentChild: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val childParent: MutableMap<String, MutableSet<String>> = mutableMapOf()

    lateinit var newNote: NoteWithLinks

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
            override suspend fun getNotes(types: List<String>): List<Note> {
               return notes.filter {
                    types.isEmpty() || types.contains(it.type)
                }.sortedBy { it.start }
            }

            override fun buildNewNote(type: NoteType, text: String, parent: Note?) {
                val startTime = generateTime(type.defaultStart, ZonedDateTime::now)
                val finishTime = generateTime(type.defaultFinish, ZonedDateTime::now)
                val note: Note by lazy {
                    if (type.tag) {
                        Note(
                            type = type.id,
                            caption = text,
                            start = startTime,
                            finish = finishTime,
                        )
                    } else {
                        Note(
                            type = type.id,
                            description = text,
                            start = startTime,
                            finish = finishTime,
                        )
                    }
                }
                newNote = if (parent == null) {
                    NoteWithLinks(note, emptySet())
                } else {
                    NoteWithLinks(note, setOf(DictionaryElement(parent)))
                }
            }

            override suspend fun getNoteForEdit(uuid: String): NoteWithLinks {
                TODO("Not yet implemented")
            }

            override suspend fun saveNote(note: Note, parents: Set<DictionaryElement>) {
                TODO("Not yet implemented")
            }

        }

    override val tagSearchService: TagSearchService
        get() = object: TagSearchService() {
            override suspend fun searchTagSuggestions(
                words: Set<String>,
                excludedTypes: Set<String>,
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
            .filter { note -> note.type == type }
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
            .filter { note -> (typeFilter.isEmpty() || typeFilter.contains(note.type)) }
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
            .filter { note -> note.type == defaultTypes[0].id && note.finish.isAfter(midnight) }
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