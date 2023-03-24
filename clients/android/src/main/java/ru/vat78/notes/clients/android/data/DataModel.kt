package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

enum class NoteType {
    NOTE, TASK, EVENT, USER, PERSON, PROJECT
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
    val uuid: UUID = UUID.randomUUID(),
    val caption: String,
    val type: String = NoteType.NOTE.toString(),
    val color: Color = Color.Transparent,
    val description: String = "",
    val start: LocalDateTime = LocalDateTime.now(),
    val finish: LocalDateTime = LocalDateTime.now()
)

