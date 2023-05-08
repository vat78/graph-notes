package ru.vat78.notes.clients.android.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.vat78.notes.clients.android.ui.components.EventCard
import ru.vat78.notes.clients.android.ui.components.TagLabel
import ru.vat78.notes.clients.android.ui.components.TagsCloud
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.time.LocalDate

@Composable
fun SingleTaskScreen(
    taskUuid: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    Column(modifier = modifier) {
        TaskMainData(taskUuid, modifier, viewModel)
        Spacer(modifier = modifier.height(4.dp))
        TaskTags(taskUuid, modifier, viewModel)
        Spacer(modifier = modifier.height(4.dp))
        TaskEvents(taskUuid, modifier, viewModel)
    }
}

@Composable
fun TaskMainData(
    taskUuid: String,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel
) {
    val task by viewModel.selectedTask.collectAsState()
    Surface(
        shape = MaterialTheme.shapes.medium,
        elevation = 1.dp
    ) {
        Column(modifier = modifier.padding(all = 8.dp)) {
            Row(modifier = modifier.fillMaxWidth()) {
                Text(
                    text = task?.caption ?: "",
                    style = MaterialTheme.typography.h1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = modifier.height(4.dp))

            Row(modifier = modifier.fillMaxWidth()) {
                Text(
                    text = task?.due?.toString() ?: defaultDueDate(),
                    style = MaterialTheme.typography.body2
                )
            }

            Spacer(modifier = modifier.height(4.dp))

            Row(modifier = modifier.fillMaxWidth()) {
                Text(
                    text = task?.description ?: "",
                    style = MaterialTheme.typography.body1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TaskTags(
    taskUuid: String,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel
) {
    val tags by viewModel.currentTags.collectAsState()
    Surface(
        shape = MaterialTheme.shapes.medium,
        elevation = 1.dp,
        modifier = modifier.fillMaxWidth().heightIn(max = 128.dp)
    ) {
        TagsCloud(modifier) {
            tags.forEach{
                TagLabel(it, modifier)
            }
        }
    }
}

@Composable
fun TaskEvents(
    taskUuid: String,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel
) {
    val events by viewModel.currentEvents.collectAsState()
    Surface(
        shape = MaterialTheme.shapes.medium,
        elevation = 1.dp
    ) {
        LazyColumn(modifier = modifier.padding(all = 4.dp).heightIn(min = (4.dp)).fillMaxWidth()) {
            items(events.size) {
                EventCard(events[it], modifier)
            }
        }
    }
}

private fun defaultDueDate(): String {
    return LocalDate.now().plusDays(7).toString()
}

@Preview
@Composable
fun TaskScreenPreview() {
    val viewModel: TasksViewModel = viewModel()
    val tasks by viewModel.tasks.collectAsState()
    viewModel.selectTask(tasks.get(0).uuid)
    GraphNotesTheme {
        SingleTaskScreen("", {})
    }
}
