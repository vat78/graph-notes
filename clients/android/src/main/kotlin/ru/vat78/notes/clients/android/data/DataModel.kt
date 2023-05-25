package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.util.*

@Immutable
data class Note(
    val type: String,
    val uuid: String = UUID.randomUUID().toString(),
    val caption: String = "",
    val color: Color = Color.Transparent,
    val description: String = "",
    val start: LocalDateTime = LocalDateTime.now(),
    val finish: LocalDateTime = LocalDateTime.now()
)

@Immutable
data class NoteWithLinks(
    val note: Note,
    val parents: Set<DictionaryElement> = emptySet(),
)

@Immutable
data class DictionaryElement(
    val id: String,
    val type: String,
    val caption: String,
    val color: Color = Color.Transparent,
) {
    constructor(note: Note): this(note.uuid, note.type, note.caption, note.color)
}

@Immutable
data class User(
    val id: String,
    val name: String,
    val email: String
)

@Immutable
data class NoteType(
    val name: String = "",
    val icon: String = "Note",
    val tag: Boolean = true,
    val hierarchical: Boolean = false,
    val symbol: String = "",
    val defaultStart: TimeDefault = TimeDefault.NOW,
    val defaultFinish: TimeDefault = TimeDefault.NOW,
    val id: String = UUID.randomUUID().toString(),
    val default: Boolean = false,
)

enum class TimeDefault {
    START_OF_TIME, PREVIOUS_NOTE, NOW, NEXT_MONTH, NEXT_YEAR, END_OF_TIME,
}

