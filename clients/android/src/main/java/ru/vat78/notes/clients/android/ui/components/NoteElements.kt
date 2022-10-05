package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.data.Event
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.Tag

@Composable
fun EventCard(event: Event, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 1.dp,
            color = event.color
        ) {
            Column(modifier = modifier.padding(all = 8.dp)) {
                Row(modifier = modifier.fillMaxWidth()) {
                    Text(
                        text = event.timestamp.toString(),
                        color = MaterialTheme.colors.secondaryVariant,
                        style = MaterialTheme.typography.subtitle2
                    )

                    Spacer(modifier = modifier.width(8.dp))

                    Text(
                        text = event.type.name,
                        color = MaterialTheme.colors.secondaryVariant,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                Spacer(modifier = modifier.height(4.dp))

                Text(
                    text = event.description,
                    modifier = modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.body2,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TagsCloud(
    modifier: Modifier,
    verticalPadding: Dp = 4.dp,
    horizontalPadding: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.padding(all = 4.dp),
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, maxWidth = minOf(constraints.maxWidth, 400)))
        }

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight) {
            var xPosition = 0
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->
                if (xPosition + placeable.width + horizontalPadding.value.toInt() > constraints.maxWidth) {
                    yPosition += placeable.height + verticalPadding.value.toInt()
                    xPosition = 0
                }

                // Position item on the screen
                placeable.placeRelative(x = xPosition, y = yPosition)

                xPosition += placeable.width + horizontalPadding.value.toInt()
            }
        }
    }
}

@Composable
fun TagLabel(tag: Tag, modifier: Modifier) {
    Column(modifier = modifier.padding(all = 2.dp)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 1.dp,
            color = tag.color,
            modifier = modifier
        ) {
            Row(modifier = modifier.padding(all = 4.dp)) {
                Icon(
                    imageVector = getIconByNoteType(tag.type),
                    contentDescription = tag.type.name
                )

                Spacer(modifier = modifier.height(4.dp))

                Text(
                    text = tag.caption,
                    modifier = modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getIconByNoteType(type: NoteType) : ImageVector {
    return Icons.Filled.Check
}