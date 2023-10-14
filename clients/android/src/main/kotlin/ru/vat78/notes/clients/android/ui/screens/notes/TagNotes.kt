package ru.vat78.notes.clients.android.ui.screens.notes

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteTypes
import ru.vat78.notes.clients.android.ui.TagListScreen
import ru.vat78.notes.clients.android.ui.TagNotesScreen
import ru.vat78.notes.clients.android.ui.components.NavigationIcon
import ru.vat78.notes.clients.android.ui.components.NewTagAlert
import ru.vat78.notes.clients.android.ui.screens.tags.views.TagTopBar
import ru.vat78.notes.clients.android.ui.screens.timeline.NoteListEvent
import ru.vat78.notes.clients.android.ui.screens.timeline.NoteListViewModel
import ru.vat78.notes.clients.android.ui.screens.timeline.views.NoteListView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagNotes(
    rootId: String,
    appState: AppState,
    onNavIconPressed: () -> Unit = { },
    onCaptionClick: (DictionaryElement) -> Unit = { },
    onNoteClick: (Note) -> Unit = { },
    onCreateNote: () -> Unit = { },
) {
    val viewModel = viewModel { NoteListViewModel(appState) }
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { appState.snackbarHostState }
    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    val tagTabs = listOf(
        NavigationIcon(Icons.Filled.List, "",
            { appState.navigate("${TagListScreen.route}/${uiState.rootNote!!.type.id}?root=$rootId") }),
        NavigationIcon(icon = Icons.Filled.Note, description = "", action = { }, selected = true)
    )

    if (uiState.newTag != null) {
        val newTag = uiState.newTag!!
        NewTagAlert(
            tag = newTag,
            error = uiState.error?.let { stringResource(it) },
            tagTypes = NoteTypes.types.values.filter { it.tag && !it.hierarchical },
            onDismiss = { viewModel.sendEvent(NoteListEvent.CancelNewTag) },
            onConfirm = { viewModel.sendEvent(NoteListEvent.CreateNewTag(it)) },
            onChangeType = { viewModel.sendEvent(NoteListEvent.ChangeNewTagType(newTag, it)) }
        )
    }
    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TagTopBar(
                caption = uiState.rootNote?.caption ?: "",
                tabs = tagTabs,
                scrollBehavior = scrollBehavior,
                onNavIconPressed = onNavIconPressed,
                onCaptionPressed = {uiState.rootNote?.let(onCaptionClick)}
            )
        },
        // Exclude ime and navigation bar padding so this can be added by the UserInput composable
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = {padding ->
            NoteListView(
                notes = uiState.notes,
                suggestions = uiState.suggestions,
                sortingType = uiState.sortingType,
                scrollState = scrollState,
                modifier = Modifier.fillMaxSize().padding(padding),
                onNoteClick = onNoteClick,
                onCreateNote = { content ->
                    viewModel.sendEvent(NoteListEvent.CreateNote(content))
                    onCreateNote()
                },
                onTagClick = {id -> appState.navigate("${TagNotesScreen.route}/$id")},
                textState = uiState.inputValue,
                onTextInput = { newInput -> viewModel.sendEvent(NoteListEvent.NewTextInput(newInput)) },
                onSelectSuggestion = { tag -> viewModel.sendEvent(NoteListEvent.SelectSuggestion(tag)) }
            )
        }
    )

    LaunchedEffect(viewModel) {
        viewModel.sendEvent(NoteListEvent.LoadNotesByParent(rootId))
    }
}