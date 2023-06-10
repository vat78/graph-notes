package ru.vat78.notes.clients.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun NoteListComponent(
    notes: List<Note>,
    noteTypeHolder: (String) -> NoteType?,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    onNoteClick: (Note) -> Unit = { },
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        LazyColumn(
            reverseLayout = false,
            state = scrollState,
            contentPadding =
                WindowInsets.statusBars.add(WindowInsets(top = 90.dp)).asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        ){
            for (index in notes.indices) {
                val prevDate = notes.getOrNull(index - 1)?.finish?.toLocalDate()
                val content = notes[index]
                val curDate = content.finish.toLocalDate()

                if (curDate != prevDate) {
                    item {
                        DayHeader(curDate)
                    }
                }

                item {
                    NoteComponent(
                        note = content,
                        noteType = noteTypeHolder.invoke(content.type),
                        onNoteClick = onNoteClick
                    )
                }
            }
        }

        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }
        JumpToBegin(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}



@Composable
fun DayHeader(day: LocalDate) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        val dayString = if (day == LocalDate.now()) "Today" else day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        SectionHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SectionHeaderLine()
    }
}

private val JumpToBottomThreshold = 56.dp

@Preview()
@Composable
fun NoteListComponentPreview() {
    val scrollState = rememberLazyListState()
    GraphNotesTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            NoteListComponent(
                notes = StubAppContext().loadNotes(null).sortedBy { it.finish },
                noteTypeHolder = { null },
                scrollState = scrollState
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
fun NoteListComponentPreviewDark() {
    val scrollState = rememberLazyListState()
    GraphNotesTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            NoteListComponent(
                notes = StubAppContext().loadNotes(null).sortedBy { it.finish },
                noteTypeHolder = { null },
                scrollState = scrollState
            )
        }
    }
}