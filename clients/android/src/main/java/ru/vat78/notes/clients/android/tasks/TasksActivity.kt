package ru.vat78.notes.clients.android.tasks

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.vat78.notes.clients.android.ui.theme.MyApplicationTheme
import java.time.LocalDate

class TasksActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TasksScreen()
                }
            }
        }
    }

    @Composable
    fun TasksScreen(modifier: Modifier = Modifier, viewModel: TasksViewModel = viewModel()) {
        Column(modifier = modifier) {
            TaskList(viewModel, modifier)
        }
    }

    @Composable
    fun TaskList(viewModel: TasksViewModel, modifier: Modifier = Modifier) {
        val tasks: List<Task> = viewModel.tasks
        LazyColumn(modifier = modifier) {
            items(tasks.size) {
                TaskCard(tasks[it], {}, modifier)
            }
        }
    }

    @Composable
    fun TaskCard(task: Task, onClick: (Task) -> Unit, modifier: Modifier = Modifier) {

        Row(
            modifier = modifier.padding(all = 4.dp)
                .fillMaxWidth()
                .clickable(enabled = true, onClick = {onClick(task)})
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 1.dp,
                color = task.color
            ) {
                Column(modifier = modifier.padding(all = 8.dp)) {
                    Row(modifier = modifier.fillMaxWidth()) {
                        Box(
                            modifier = modifier.size(10.dp).align(Alignment.CenterVertically)
                                .clip(RoundedCornerShape(3.dp))
                                .background(getColorByDate(task.due))
                        )

                        Spacer(modifier = modifier.width(8.dp))

                        Text(
                            text = task.due.toString(),
                            color = MaterialTheme.colors.secondaryVariant,
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                    Spacer(modifier = modifier.height(4.dp))


                    Text(
                        text = task.caption,
                        modifier = modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }

    private fun getColorByDate(date: LocalDate) : Color {
        return when {
            LocalDate.now().isAfter(date) -> Color.Red
            LocalDate.now().equals(date) -> Color.Magenta
            LocalDate.now().plusDays(3).isAfter(date) -> Color.Yellow
            LocalDate.now().plusDays(7).isAfter(date) -> Color.Green
            else -> Color.LightGray
        }
    }


    @Preview(name = "Light Mode")
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark Mode"
    )
    @Composable
    fun PreviewMessageCard() {
        MyApplicationTheme {
            Surface {
                TaskCard(
                    task = Task(
                        LocalDate.now().plusDays(6),
                        "Take a look at Jetpack Compose, it's great!",
                        Color.Transparent
                    ),
                    {}
                )
            }
        }
    }
}