package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
