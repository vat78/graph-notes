package ru.vat78.notes.clients.android.tags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.notes.ListState
import ru.vat78.notes.clients.android.notes.NoteShort
import ru.vat78.notes.clients.android.notes.NotesTopBar
import ru.vat78.notes.clients.android.notes.SmallNoteEditor
import ru.vat78.notes.clients.android.ui.components.JumpToBegin
import ru.vat78.notes.clients.android.ui.components.SectionHeaderLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagListContent(
    type: String,
    rootId: String?,
    viewModel: TagsViewModel,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { },
    onCaptionClick: (Note) -> Unit = { },
    onNoteClick: (Note) -> Unit = { },
    onCreateNote: () -> Unit = { },
) {
    val uiState by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    if (uiState.state == ListState.INIT ) {
        viewModel.sendEvent(TagsUiEvent.LoadData(type, rootId))
    }

    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Tags(
                    uiState.tags,
                    noteTypesHolder = {uiState.noteTypes[it]},
                    modifier = Modifier.weight(1f),
                    scrollState = scrollState,
                    onNoteClick = onNoteClick
                )
                SmallNoteEditor(
                    onEventInput = { content ->
                        viewModel.sendEvent(TagsUiEvent.CreateTag(type = uiState.tagType!!, caption = content, parent = uiState.rootNote))
                        onCreateNote()
                    },
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
            NotesTopBar(
                caption = uiState.caption,
                onNavIconPressed = onNavIconPressed,
                scrollBehavior = scrollBehavior,
                modifier = Modifier.statusBarsPadding(),
                onCaptionPressed = {
                    val curNote = uiState.rootNote
                    if (curNote != null) {
                        onCaptionClick.invoke(curNote)
                    }
                }
            )
        }
    }
}

@Composable
fun Tags(
    notes: List<Note>,
    noteTypesHolder: (String) -> NoteType?,
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
                val prevLetter = ""
                val content = notes[index]
                val curLetter = if (content.caption.isBlank()) "" else content.caption[0].uppercase()

                if (curLetter != prevLetter) {
                    item {
                        LetterHeader(curLetter)
                    }
                }

                item {
                    NoteShort(
                        note = content,
                        noteType = noteTypesHolder.invoke(content.type),
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
fun LetterHeader(letter: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        SectionHeaderLine()
        Text(
            text = letter,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SectionHeaderLine()
    }
}

private val JumpToBottomThreshold = 56.dp