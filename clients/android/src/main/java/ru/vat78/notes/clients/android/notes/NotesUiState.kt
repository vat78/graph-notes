package ru.vat78.notes.clients.android.notes

import androidx.compose.runtime.toMutableStateList
import ru.vat78.notes.clients.android.data.Note

class NotesUiState(
    val caption: String,
    initialNotes: List<Note>
) {

    private val _notes: MutableList<Note> = initialNotes.toMutableStateList()
    val notes: List<Note> = _notes

    fun addNote(note: Note) {
        _notes.add(0, note)
    }
}