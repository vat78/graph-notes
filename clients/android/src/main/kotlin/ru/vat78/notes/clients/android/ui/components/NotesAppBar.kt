@file:OptIn(ExperimentalMaterial3Api::class)

package ru.vat78.notes.clients.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@Composable
fun NotesAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        actions = actions,
        title = title,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            MenuIcon(onClick = onNavIconPressed)
        }
    )
}

@Preview
@Composable
fun NotesAppBarPreview() {
    GraphNotesTheme {
        NotesAppBar(title = { Text("Preview!") })
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun NotesAppBarPreviewDark() {
    GraphNotesTheme{
        NotesAppBar(title = { Text("Preview!") })
    }
}