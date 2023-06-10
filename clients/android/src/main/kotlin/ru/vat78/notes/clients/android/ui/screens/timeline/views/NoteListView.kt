package ru.vat78.notes.clients.android.ui.screens.timeline.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.ui.components.NoteListComponent
import ru.vat78.notes.clients.android.ui.components.SmallNoteEditor
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@Composable
fun NoteListView(
    notes: List<Note>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    onNoteClick: (Note) -> Unit = { },
    onCreateNote: (String) -> Unit = { }
) {
    val scope = rememberCoroutineScope()
    Column(modifier = modifier) {
        NoteListComponent(
            notes,
            noteTypeHolder = { null },
            modifier = Modifier.weight(1f),
            scrollState = scrollState,
            onNoteClick = onNoteClick
        )
        SmallNoteEditor(
            onEventInput = onCreateNote,
            resetScroll = {
                scope.launch {
                    scrollState.scrollToItem(0)
                }
            },
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding(),
        )
    }
}

@Preview
@Composable
fun NoteListViewPreview() {
    val scrollState = rememberLazyListState()
    GraphNotesTheme {
        NoteListView(
            notes = listOf(
                Note(caption = "test 1"),
                Note(caption = "test 2")
            ),
            scrollState = scrollState)
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun NoteListViewPreviewDark() {
    val scrollState = rememberLazyListState()
    GraphNotesTheme {
        NoteListView(notes = emptyList(), scrollState = scrollState)
    }
}