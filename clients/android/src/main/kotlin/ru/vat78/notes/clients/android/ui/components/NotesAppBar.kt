@file:OptIn(ExperimentalMaterial3Api::class)

package ru.vat78.notes.clients.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Composable
fun AppBarTabs(
    tabs: List<NavigationIcon>,
    modifier: Modifier = Modifier
) {
    if (tabs.isNotEmpty()) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp

        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (tab in tabs) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        tonalElevation = if (tab.selected) 36.dp else 0.dp
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.description,
                            modifier = Modifier.clickable {
                                tab.action.invoke()
                            },
                        )
                    }
                }
            }
        }
    }
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