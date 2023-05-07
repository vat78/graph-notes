package ru.vat78.notes.clients.android.data

import java.time.LocalDateTime

class NotesStorage {
    val notes: MutableList<Note> = arrayListOf(
        Note(caption = "test 1"),
        Note(caption = "test 2"),
        Note(caption = "test formatted", start = LocalDateTime.of(2023, 3, 23, 18, 55), description = "shfsh ;ljrg sdfbgsn @test fl;cvbmvcx fdsdfblmkb sbksdlkb sbnlskdgb dsklbnlksdbn  sdfbldskblksd dsbkmdsblksm sdfkblmskldbm sdbmsldkbm  sdmbflkdsmbsl dsbmlkdsmb sdbklmsldkbms sdmflbsld"),
        Note(uuid = "test-uuid", caption = "test 3", start = LocalDateTime.of(2023, 3, 22, 18, 55), description = "dlfak *vblmafbvmf afbmafdbma* abfdbbadabd"),
        Note(caption = "test 4", start = LocalDateTime.of(2023, 3, 22, 18, 0)),
        Note(caption = "test org", type = NoteType.ORGANISATION),
        Note(caption = "super org", type = NoteType.ORGANISATION),
    )

    private val parentChild: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val childParent: MutableMap<String, MutableSet<String>> = mutableMapOf()

    lateinit var newNote: Note

    fun loadNotes(type: NoteType?) : List<Note> {
        if (type == null) {
            return notes.toList()
        }
        return notes
            .filter { note -> note.type == type }
    }

    fun newNote(note: Note) {
        newNote = note
    }

    fun getOneNote(uuid: String): Note {
        if (uuid == "new") {
            return newNote
        }
        return notes
            .asSequence()
            .filter { note -> note.uuid == uuid }
            .first()
    }

    fun saveNote(note: Note) {
        newNote = note
        val existingNote = notes.find { n -> n.uuid == note.uuid }
        if (existingNote != null) {
            notes.remove(existingNote)
        }
        notes.add(note)
        notes.sortBy { n -> n.start }
    }

    fun getAllByName(name: String, typeFilter: NoteType?, timeFilter: Boolean): List<DictionaryElement> {
        if (name.length < 3) {
            return listOf()
        }
        return notes
            .asSequence()
            .filter { note -> note.type.caption && (typeFilter == null || note.type == typeFilter) }
            .filter { note -> !timeFilter || (note.start.isAfter(LocalDateTime.now()) && note.finish.isBefore(LocalDateTime.now())) }
            .filter { note -> note.caption.contains(name, ignoreCase = true) }
            .map { note -> DictionaryElement(note) }
            .take(7)
            .toList()
    }

    fun getClosestTimeOrDefault(default: LocalDateTime): LocalDateTime {
        val midnight = default.toLocalDate().atStartOfDay()
        return notes
            .asSequence()
            .filter { note -> note.type == NoteType.NOTE && note.finish.isAfter(midnight) }
            .maxOfOrNull { note -> note.finish } ?: default
    }

    fun getParent(uuid: String): DictionaryElement? {
        return childParent[uuid]?.firstOrNull()?.let { DictionaryElement(getOneNote(it)) }
    }

    fun getLinks(uuid: String): Set<DictionaryElement> {
        return childParent[uuid]?.map { DictionaryElement(getOneNote(it)) }?.toSet() ?: setOf()
    }

    fun saveLinks(root: String, links: Set<DictionaryElement>) {
        childParent[root] = links.map { it.uuid }.toMutableSet()
        links.forEach { link ->
            (parentChild[link.uuid] ?: mutableSetOf()).add(root)
        }
    }

    fun addLink(parent: String, child: String) {
        (parentChild[parent] ?: mutableSetOf()).add(child)
        (childParent[child] ?: mutableSetOf()).add(parent)
    }
}