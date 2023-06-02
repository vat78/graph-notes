package ru.vat78.notes.clients.android.tags

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagListContent(
    type: String,
    viewModel: TagsViewModel,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { },
    onNoteClick: (String) -> Unit = { },
    onCreateNote: () -> Unit = { },
) {
    val uiState by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    viewModel.sendEvent(TagsUiEvent.LoadData(type, null))
}