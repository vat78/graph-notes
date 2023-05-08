package ru.vat78.notes.clients.android.tasks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.data.Event
import ru.vat78.notes.clients.android.data.GraphDataRepository
import ru.vat78.notes.clients.android.data.Tag
import ru.vat78.notes.clients.android.data.TaskDetails
import ru.vat78.notes.clients.android.data.TaskShortView

class TasksViewModel constructor(
    private val repository: GraphDataRepository = GraphDataRepository()
): ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskShortView>>(emptyList())
    private val _selectedTask = MutableStateFlow<TaskDetails?>(null)
    private val _tagsOfSelectedTask = MutableStateFlow<List<Tag>>(emptyList())
    private val _eventsOfSelectedTask = MutableStateFlow<List<Event>>(emptyList())

    val tasks: StateFlow<List<TaskShortView>>
        get() = _tasks
    val selectedTask: StateFlow<TaskDetails?>
        get() = _selectedTask

    val currentTags: StateFlow<List<Tag>>
        get() = _tagsOfSelectedTask

    val currentEvents: StateFlow<List<Event>>
        get() = _eventsOfSelectedTask

    init {
        _tasks.value = repository.allTasks()
    }

    fun selectTask(uuid: String) {
        _selectedTask.value = repository.taskByUuid(uuid)
        _tagsOfSelectedTask.value = repository.tagsByTaskUuid(uuid)
        _eventsOfSelectedTask.value = repository.eventsByTaskUuid(uuid)
    }
}