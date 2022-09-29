package ru.vat78.notes.clients.android.tasks

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import ru.vat78.notes.clients.android.data.GraphDataRepository
import java.time.LocalDate
import java.util.UUID

class TasksViewModel constructor(
    private val repository: GraphDataRepository = GraphDataRepository()
): ViewModel() {

    val tasks: List<Task>
        get() = repository.tasks

}

data class Task(
    val due: LocalDate,
    val caption: String,
    val color: Color = Color.Transparent,
    val projects: List<String> = emptyList(),
    val uuid: String = UUID.randomUUID().toString(),
)