package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.R

data class NavigationIcon(
    val icon: ImageVector,
    val description: String,
    val action: () -> Unit,
    val selected: Boolean = false
)

@Composable
fun SearchIcon(
    onClick: () -> Unit = {}
) {
    Icon(
        imageVector = Icons.Outlined.Search,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(24.dp),
        contentDescription = stringResource(id = R.string.search)
    )
}

@Composable
fun InfoIcon(
    onClick: () -> Unit = {}
) {
    Icon(
        imageVector = Icons.Outlined.Info,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(24.dp),
        contentDescription = stringResource(id = R.string.info)
    )
}

@Composable
fun MenuIcon(
    onClick: () -> Unit = {}
) {
    Icon(
        ImageVector.vectorResource(id = R.drawable.ic_menu_24dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        contentDescription = stringResource(id = R.string.menu),
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick)
            .padding(16.dp)
    )
}