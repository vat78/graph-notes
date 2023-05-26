package ru.vat78.notes.clients.android.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

val TmpIcons : Map<String, ImageVector> = mapOf(
    Pair("Note", Icons.Filled.Note),
    Pair("Check", Icons.Filled.Check),
    Pair("Tag", Icons.Filled.Tag),
    Pair("People", Icons.Filled.People),
    Pair("House", Icons.Filled.House),
    Pair("Phone", Icons.Filled.Phone),
)

val defaultTypes = listOf(
    NoteType(
        name = "note",
        icon = "Note",
        tag = false,
        hierarchical = false,
        symbol = " ",
        defaultStart = TimeDefault.PREVIOUS_NOTE,
        defaultFinish = TimeDefault.NOW,
        default = true
    ),
    NoteType(
        name = "task",
        icon = "Check",
        tag = true,
        hierarchical = true,
        symbol = "@",
        defaultStart = TimeDefault.NOW,
        defaultFinish = TimeDefault.NEXT_MONTH
    ),
    NoteType(
        name = "tag",
        icon = "Tag",
        tag = true,
        hierarchical = false,
        symbol = "#",
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
    NoteType(
        name = "person",
        icon = "People",
        tag = true,
        hierarchical = false,
        symbol = "&",
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
    NoteType(
        name = "organisation",
        icon = "House",
        tag = true,
        hierarchical = false,
        symbol = "#",
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
    NoteType(
        name = "phone",
        icon = "Phone",
        tag = true,
        hierarchical = false,
        symbol = "+",
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
)

fun generateTime(rule: TimeDefault, previousFunction: () -> ZonedDateTime) : ZonedDateTime {
    return when (rule) {
        TimeDefault.START_OF_TIME -> ZonedDateTime.of(LocalDateTime.of(1970, 1, 1, 0, 0), ZoneOffset.UTC)
        TimeDefault.PREVIOUS_NOTE -> previousFunction.invoke()
        TimeDefault.NOW -> ZonedDateTime.now()
        TimeDefault.NEXT_MONTH -> ZonedDateTime.of(LocalDateTime.now().plusMonths(1).withDayOfMonth(1), ZoneOffset.UTC)
        TimeDefault.NEXT_YEAR -> ZonedDateTime.of(LocalDateTime.now().plusYears(1).withDayOfYear(1), ZoneOffset.UTC)
        TimeDefault.END_OF_TIME -> ZonedDateTime.of(LocalDateTime.of(2999, 12, 31, 23, 59), ZoneOffset.UTC)
    }
}