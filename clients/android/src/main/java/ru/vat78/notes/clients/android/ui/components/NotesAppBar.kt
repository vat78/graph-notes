@file:OptIn(ExperimentalMaterial3Api::class)

package ru.vat78.notes.clients.android.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    val backgroundColor = lerp(
        backgroundColors.containerColor(colorTransitionFraction = 0f).value,
        backgroundColors.containerColor(colorTransitionFraction = 1f).value,
        FastOutLinearInEasing.transform(scrollBehavior?.state?.overlappedFraction ?: 0f)
    )

    val foregroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent
    )
    Box(modifier = Modifier.background(backgroundColor)) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            actions = actions,
            title = title,
            scrollBehavior = scrollBehavior,
            colors = foregroundColors,
            navigationIcon = {
                MenuIcon(onClick = onNavIconPressed)
            }
        )
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
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun NotesAppBarPreviewDark() {
    GraphNotesTheme{
        NotesAppBar(title = { Text("Preview!") })
    }
}