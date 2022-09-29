package ru.vat78.notes.clients.android.data

import androidx.compose.ui.graphics.Color
import ru.vat78.notes.clients.android.tasks.Task
import java.time.LocalDate

class GraphDataRepository {

    private val _tasks: List<Task> = mutableListOf(
        Task(LocalDate.now(), "Today's task"),
        Task(LocalDate.now().minusDays(1), "Yesterday's task"),
        Task(LocalDate.now().plusDays(2), "After tomorrow and green", Color.Green),
        Task(LocalDate.now().plusDays(1), "Tomorrow's task"),
        Task(LocalDate.now().plusDays(7), "Next week")
    )

    val tasks: List<Task>
        get() = _tasks

    fun taskByUuid(uuid: String) : Task? {
        return _tasks.find { it.uuid == uuid }
    }
}