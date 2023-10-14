package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.data.getIcon
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.util.*

@ExperimentalFoundationApi
@Composable
fun TagArea(
    tags: Set<DictionaryElement>,
    modifier: Modifier = Modifier,
    onClickTag: (DictionaryElement) -> Unit = {},
    onDeleteTag: (DictionaryElement) -> Unit = {},
) {

    val tagForDeletion: MutableState<Optional<DictionaryElement>> = remember { mutableStateOf(Optional.empty()) }
    val componentWeight = remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Surface(
        modifier = modifier.fillMaxWidth()
            .onGloballyPositioned {
                componentWeight.value = with(density) {
                it.size.width.toDp()
            }
        }
    ) {
        if (tagForDeletion.value .isPresent) {
            val caption = tagForDeletion.value.get().caption
            AlertDialog(
                onDismissRequest = { tagForDeletion.value = Optional.empty()},
                confirmButton = {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Press to delete link to '${caption}'?",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clickable {
                                onDeleteTag.invoke(tagForDeletion.value.get())
                                tagForDeletion.value = Optional.empty()
                            }
                        )
                    }
                },
            )
        }
        TagTable(
            tags = tags.toList(),
            weight = componentWeight.value,
            modifier = Modifier.padding(all = 2.dp),
            onClickTag = onClickTag,
            onDeleteTag = onDeleteTag
        )
//        TagsCloud(modifier = modifier) {
//            tags.forEach {
//                Column(modifier = modifier.padding(all = 2.dp)) {
//                    Surface(
//                        shape = MaterialTheme.shapes.medium,
//                        elevation = 1.dp,
//                        color = it.color,
//                        modifier = modifier.combinedClickable(
//                            onClick = {},
//                            onLongClick = {
//                                tagForDeletion.value = Optional.of(it)
//                            }
//                        )
//                    ) {
//                        Text(
//                            text = cutText(it.caption, 40),
//                            textAlign = TextAlign.Center,
//                            style = MaterialTheme.typography.overline,
//                            modifier = Modifier.padding(4.dp),
//                            maxLines = 1
//                        )
//                    }
//                }
//            }
//        }
    }
}

@ExperimentalFoundationApi
@Composable
fun TagTable(
    tags: List<DictionaryElement>,
    weight: Dp,
    modifier: Modifier = Modifier,
    onClickTag: (DictionaryElement) -> Unit = {},
    onDeleteTag: (DictionaryElement) -> Unit = {},
) {
   Column(modifier = modifier) {
       var i = 0
       while (i < tags.size) {
           Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
               var curLength = 0
               while (i < tags.size) {
                   val tag = tags[i]
                   val text = cutText(tag.caption, 40)
                   if (curLength > 0 && text.length > 30 - curLength) {
                       break
                   }
                   curLength += text.length
                   i++

                   Card(
                       shape = MaterialTheme.shapes.medium,
                       elevation = CardDefaults.cardElevation(
                           defaultElevation = 2.dp
                       ),
                       colors = CardDefaults.cardColors(containerColor = tag.color),
                       modifier = modifier.combinedClickable(
                           onClick = {
                               onClickTag.invoke(tag)
                           },
                           onLongClick = {
                               onDeleteTag.invoke(tag)
                           }
                       )
                   ) {
                       TagRecord(
                           text = text,
                           icon = getIcon(tag.type),
                           modifier = Modifier.background(Color.Transparent)
                       )
                   }
               }
           }
       }
   }
}

@Composable
fun TagRecord(
    text: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier
) {
    Row (modifier = modifier) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, top = 8.dp)
                    .height(18.dp)
            )
        }
        Text(
            text = text,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall.copy(
                color = LocalContentColor.current,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

private fun cutText(text: String, maxLength: Int): String {
    return if (text.length > maxLength) {
        "${text.substring(0, maxLength - 3)}..."
    } else {
        text
    }
}

@Preview
@ExperimentalFoundationApi
@Composable
fun ViewTagArea() {
    val types = runBlocking { StubAppContext().noteTypeStorage.getTypes() }.toList()
    GraphNotesTheme {
        TagArea(
            tags = setOf(DictionaryElement(id = "1", type = types[1], caption = "test 1"),
                DictionaryElement(id = "2", type = types[2], caption = "test 2"),
                DictionaryElement(id = "3", type = types[1], caption = "test 3", color = Color.Red),
                DictionaryElement(id = "4", type = types[1], caption = "test with very long long long 3", color = Color.Red),
                DictionaryElement(id = "5", type = types[1], caption = "test with very long long long long long", color = Color.Red),
                DictionaryElement(id = "6", type = types[1], caption = "test with very long long long long long log long long text"),
                DictionaryElement(id = "7", type = types[1], caption = "test 1111111"),
                DictionaryElement(id = "8", type = types[1], caption = "test 22222222222")
            ),
            modifier = Modifier.background(Color.Gray)
        )
    }
}