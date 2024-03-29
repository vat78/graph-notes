package ru.vat78.notes.clients.android.ui.screens.tags.views

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ru.vat78.notes.clients.android.ui.components.AppBarTabs
import ru.vat78.notes.clients.android.ui.components.InfoIcon
import ru.vat78.notes.clients.android.ui.components.NavigationIcon
import ru.vat78.notes.clients.android.ui.components.NotesAppBar
import ru.vat78.notes.clients.android.ui.components.SearchIcon
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagTopBar(
    caption: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    tabs: List<NavigationIcon> = emptyList(),
    onNavIconPressed: () -> Unit = { },
    onCaptionPressed: () -> Unit = { },
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            NotesAppBar(
                scrollBehavior = scrollBehavior,
                onNavIconPressed = onNavIconPressed,
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = caption,
                            textAlign = TextAlign.Center,
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
            AppBarTabs(tabs, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TagTopBarPreview() {
    GraphNotesTheme {
        TagTopBar(
            caption = "Preview!",
            tabs = listOf(NavigationIcon(Icons.Filled.List, "", {}, true),
                NavigationIcon(Icons.Filled.Note, "", {}))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun TagTopBarPreviewDark() {
    GraphNotesTheme{
        TagTopBar(
            caption = "Preview!",
            tabs = listOf(NavigationIcon(Icons.Filled.List, "", {}, true),
                NavigationIcon(Icons.Filled.Note, "", {}))
        )
    }
}