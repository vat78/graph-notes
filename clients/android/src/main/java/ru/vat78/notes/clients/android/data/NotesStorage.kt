package ru.vat78.notes.clients.android.data

import java.time.LocalDateTime

class NotesStorage {
    val notes: MutableList<Note> = arrayListOf(
        Note(caption = "test 1"),
        Note(caption = "test 2"),
        Note(caption = "test formatted", start = LocalDateTime.of(2023, 3, 23, 18, 55), description = "shfsh ;ljrg sdfbgsn @test fl;cvbmvcx fdsdfblmkb sbksdlkb sbnlskdgb dsklbnlksdbn  sdfbldskblksd dsbkmdsblksm sdfkblmskldbm sdbmsldkbm  sdmbflkdsmbsl dsbmlkdsmb sdbklmsldkbms sdmflbsld"),
        Note(caption = "test 3", start = LocalDateTime.of(2023, 3, 22, 18, 55), description = "dlfak *vblmafbvmf afbmafdbma* abfdbbadabd"),
        Note(caption = "test 4", start = LocalDateTime.of(2023, 3, 22, 18, 0))
    )

    lateinit var newNote: Note

    fun loadNotes(type: NoteType?) : List<Note> {
        if (type == null) {
            return notes.toList()
        }
        return notes
            .filter { note -> note.type == type.name }
    }

    fun newNote(note: Note) {
        newNote = note
    }

    fun getOneNote(uuid: String): Note {
        if (uuid == "new") {
            return newNote
        }
        return notes
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
}