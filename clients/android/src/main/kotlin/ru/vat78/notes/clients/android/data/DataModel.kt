package ru.vat78.notes.clients.android.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


enum class NoteTypeStructure {
    NONE, HIERARCHY, INTERNAL_TAGS, ANY_TAGS
}

enum class NoteType(
    val icon: ImageVector,
    val caption: Boolean = true,
    val description: Boolean = false,
    val time: Boolean = true,
    val structure: NoteTypeStructure = NoteTypeStructure.NONE

) {
    NOTE (Icons.Filled.Note, caption = true, description = true, time = true, structure = NoteTypeStructure.ANY_TAGS),
    TASK (Icons.Filled.Check, caption = true, description = false, time = true, structure = NoteTypeStructure.HIERARCHY),
    TAG(Icons.Filled.Tag, caption = true, description = false, time = false, structure = NoteTypeStructure.INTERNAL_TAGS),
    USER (Icons.Filled.Person, caption = true, description = false, time = false, structure = NoteTypeStructure.NONE),
    PERSON(Icons.Filled.People, caption = true, description = false, time = false, structure = NoteTypeStructure.NONE),
    ORGANISATION(Icons.Filled.House, caption = true, description = false, time = false, structure = NoteTypeStructure.HIERARCHY)
}

data class Tag(
    val type: NoteType,
    val caption: String,
    val color: Color
)

@Immutable
data class Event(
    val type: NoteType,
    val description: String,
    val timestamp: LocalDateTime,
    val color: Color = Color.Transparent
)

data class TaskShortView(
    val due: LocalDate,
    val caption: String,
    val color: Color = Color.Transparent,
    val projects: List<String> = emptyList(),
    val uuid: String = UUID.randomUUID().toString(),
)

data class TaskDetails(
    val due: LocalDate,
    val caption: String,
    val description: String = "",
    val color: Color = Color.Transparent,
    val uuid: String = UUID.randomUUID().toString(),
)

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
data class ObjectType(
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

