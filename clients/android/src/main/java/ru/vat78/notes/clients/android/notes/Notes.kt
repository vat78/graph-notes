package ru.vat78.notes.clients.android.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.ui.components.FunctionalityNotAvailablePopup
import ru.vat78.notes.clients.android.ui.components.InfoIcon
import ru.vat78.notes.clients.android.ui.components.JumpToBegin
import ru.vat78.notes.clients.android.ui.components.NotesAppBar
import ru.vat78.notes.clients.android.ui.components.SearchIcon
import ru.vat78.notes.clients.android.ui.components.SymbolAnnotationType
import ru.vat78.notes.clients.android.ui.components.messageFormatter
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListContent(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { }
) {

    val uiState by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topBarState) }
    val scope = rememberCoroutineScope()

    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Notes(
                    uiState.notes,
                    modifier = Modifier.weight(1f),
                    scrollState = scrollState
                )
                SmallNoteEditor(
                    onEventInput = { content ->
                        viewModel.sendEvent(NotesUiEvent.CreateNote(content))
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
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(
    caption: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { }
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }

    NotesAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onNavIconPressed,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        actions = {
            SearchIcon(onClick = { functionalityNotAvailablePopupShown = true })
            InfoIcon(onClick = { functionalityNotAvailablePopupShown = true })
        }
    )
}

@Composable
fun Notes(
    notes: List<Note>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
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
                val prevDate = notes.getOrNull(index - 1)?.start?.toLocalDate()
                val content = notes[index]
                val curDate = content.start.toLocalDate()

                if (curDate != prevDate) {
                    item {
                        DayHeader(curDate)
                    }
                }

                item {
                    NoteShort(
                        caption = content.caption,
                        description = content.description,
                        time = content.start
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
fun NoteShort(
    caption: String,
    description: String,
    time: LocalDateTime,
    icon: ImageVector = Icons.Filled.Info,
    color: Color = MaterialTheme.colorScheme.tertiary
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(description)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row (modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp)
                            .height(18.dp)
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = caption,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = LocalContentColor.current,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                        Surface (
                            color = color,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LocalContentColor.current,
                                    fontStyle = FontStyle.Italic
                                ),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                if (description.isNotBlank()) {
                    ClickableText(
                        text = styledMessage,
                        maxLines = 4,
                        style = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                        onClick = {
                            styledMessage
                                .getStringAnnotations(start = it, end = it)
                                .firstOrNull()
                                ?.let { annotation ->
                                    when (annotation.tag) {
                                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                                        SymbolAnnotationType.TAG.name -> {}
                                        else -> Unit
                                    }
                                }
                        }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun DayHeader(day: LocalDate) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        val dayString = if (day == LocalDate.now()) "Today" else day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    Divider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

private val JumpToBottomThreshold = 56.dp


@Preview
@Composable
fun NotesPreview() {
    GraphNotesTheme {
        NoteListContent(
            viewModel = NotesViewModel()
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarPrev() {
    GraphNotesTheme {
        NotesTopBar(caption = "composers")
    }
}