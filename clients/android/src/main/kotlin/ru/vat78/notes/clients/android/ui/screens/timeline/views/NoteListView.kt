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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.ui.components.NoteListComponent
import ru.vat78.notes.clients.android.ui.components.SmallNoteEditor
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@Composable
fun NoteListView(
    notes: List<Note>,
    suggestions: List<DictionaryElement>,
    scrollState: LazyListState,
    textState: TextFieldValue,
    modifier: Modifier = Modifier,
    onNoteClick: (Note) -> Unit = { },
    onCreateNote: (String) -> Unit = { },
    onTagClick: (String) -> Unit = { },
    onTextInput: (TextFieldValue) -> Unit = {},
    onSelectSuggestion: (DictionaryElement) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    Column(modifier = modifier) {
        NoteListComponent(
            notes,
            modifier = Modifier.weight(1f),
            scrollState = scrollState,
            onNoteClick = onNoteClick,
            onTagClick = onTagClick
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
            textState = textState,
            suggestions = suggestions,
            onSelectSuggestion = onSelectSuggestion,
            onTextChanged = onTextInput
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
            suggestions = emptyList(),
            scrollState = scrollState,
            textState = TextFieldValue("")
        )
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
        NoteListView(
            notes = emptyList(),
            suggestions = emptyList(),
            scrollState = scrollState,
            textState = TextFieldValue("")
        )
    }
}