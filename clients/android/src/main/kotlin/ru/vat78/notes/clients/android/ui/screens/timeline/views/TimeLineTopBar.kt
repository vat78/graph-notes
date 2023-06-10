package ru.vat78.notes.clients.android.ui.screens.timeline.views

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.vat78.notes.clients.android.ui.components.InfoIcon
import ru.vat78.notes.clients.android.ui.components.NotesAppBar
import ru.vat78.notes.clients.android.ui.components.SearchIcon
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLineTopBar(
    caption: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    onCaptionPressed: () -> Unit = { },
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        NotesAppBar(
            scrollBehavior = scrollBehavior,
            onNavIconPressed = onNavIconPressed,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable(onClick = onCaptionPressed)
                    )
                }
            },
            actions = {
                SearchIcon(onClick = { })
                InfoIcon(onClick = { })
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TimeLineTopBarPreview() {
    GraphNotesTheme {
        TimeLineTopBar(caption = "Preview!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun TimeLineTopBarPreviewDark() {
    GraphNotesTheme{
        TimeLineTopBar(caption = "Preview!")
    }
}