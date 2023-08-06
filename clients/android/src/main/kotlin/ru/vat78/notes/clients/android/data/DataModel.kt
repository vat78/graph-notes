package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

@Immutable
data class Note(
    val type: NoteType = NoteType(),
    val id: String = UUID.randomUUID().toString(),
    val caption: String = "",
    val color: Color = Color.Transparent,
    val description: String = "",
    val start: ZonedDateTime = ZonedDateTime.now(),
    val finish: ZonedDateTime = ZonedDateTime.now(),
    val root: Boolean = false,
    val textInsertions: Map<String, DictionaryElement> = emptyMap(),
    val lastUpdate: Long = Instant.now().epochSecond,
    val deleted: Boolean = false
)

@Immutable
data class NoteLink(
    val parentId: String,
    val childId: String,
    val deleted: Boolean,
    val lastUpdate: Long = Instant.now().epochSecond
)

@Immutable
data class NoteWithParents(
    val note: Note,
    val parents: Set<DictionaryElement> = emptySet(),
)

@Immutable
data class NoteWithChildren(
    val note: Note,
    val children: List<String> = emptyList(),
)

@Immutable
data class DictionaryElement(
    val id: String,
    val type: NoteType,
    val caption: String = "",
    val color: Color = Color.Transparent,
) {
    constructor(note: Note): this(note.id, note.type, note.caption, note.color)
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
    val symbol: Char = '#',
    val defaultStart: TimeDefault = TimeDefault.NOW,
    val defaultFinish: TimeDefault = TimeDefault.NOW,
    val id: String = UUID.randomUUID().toString(),
    val default: Boolean = false,
)

enum class TimeDefault {
    START_OF_TIME, PREVIOUS_NOTE, NOW, NEXT_MONTH, NEXT_YEAR, END_OF_TIME,
}

