package ru.vat78.notes.clients.android.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

class GraphDataRepository {

    private val _tasks: List<TaskDetails> = mutableListOf(
        TaskDetails(LocalDate.now(), "Today's task", "test description with many many many many many many many many many many many many many many many many many many many many many words"),
        TaskDetails(LocalDate.now().minusDays(1), "Yesterday's task"),
        TaskDetails(LocalDate.now().plusDays(2), "After tomorrow and green", color = Color.Green),
        TaskDetails(LocalDate.now().plusDays(1), "Tomorrow's task"),
        TaskDetails(LocalDate.now().plusDays(7), "Next week")
    )
    private val _tags: MutableMap<String, List<Tag>> = mutableMapOf()
    private val _events: MutableMap<String, List<Event>> = mutableMapOf()

    fun allTasks(): List<TaskShortView> {
        if (_tags.isEmpty()) fillData()
        return _tasks
            .map { t -> TaskShortView(
                due = t.due,
                caption = t.caption,
                color = t.color,
                projects = emptyList(),
                uuid = t.uuid
            )}
            .toList()
    }

    private fun fillData() {
        _tasks.forEach {
            _tags.put(it.uuid, generateTags())
            _events.put(it.uuid, generateEvents())
        }
    }

    private fun generateTags(): MutableList<Tag> {
        return (0..(Random.nextInt(10))).map {
            val type = randomType()
            Tag(type, "some text" + "1".repeat(Random.nextInt(10)), colorOfType(type))
        }.toMutableList()
    }

    private fun generateEvents(): MutableList<Event> {
        return (0..(Random.nextInt(7))).map {
            val type = randomType()
            Event(type, "some event text", LocalDateTime.now().minusDays(Random.nextLong(7)), colorOfType(type))
        }.filter { it.type == NoteType.NOTE || it.type == NoteType.EVENT }.toMutableList()
    }

    private fun randomType(): NoteType {
        return when(Random.nextInt(5)) {
            0 -> NoteType.NOTE
            1 -> NoteType.TASK
            2 -> NoteType.EVENT
            3 -> NoteType.PERSON
            else -> NoteType.PROJECT
        }
    }

    private fun colorOfType(type: NoteType): Color {
        return when(type) {
            NoteType.NOTE -> Color.Gray
            NoteType.TASK -> Color.Yellow
            NoteType.EVENT -> Color.LightGray
            NoteType.PROJECT -> Color.Green
            else -> Color.White
        }
    }

    fun taskByUuid(uuid: String) : TaskDetails? {
        return _tasks.find { it.uuid == uuid }
    }

    fun tagsByTaskUuid(uuid: String): List<Tag> {
        return _tags.getOrDefault(uuid, emptyList()).sortedBy { it.type }
    }

    fun eventsByTaskUuid(uuid: String): List<Event> {
        return _events.getOrDefault(uuid, emptyList()).sortedByDescending { it.timestamp }
    }
}