package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.data.DictionaryElement
import java.util.*

@ExperimentalFoundationApi
@Composable
fun TagArea(
    tags: Set<DictionaryElement>,
    modifier: Modifier = Modifier,
    onDeleteTag: (DictionaryElement) -> Unit = {},
) {

    val tagForDeletion: MutableState<Optional<DictionaryElement>> = remember { mutableStateOf(Optional.empty()) }

    Surface(modifier.fillMaxWidth().heightIn(min = 64.dp)) {
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
        TagsCloud(modifier = modifier) {
            tags.forEach {
                Column(modifier = modifier.padding(all = 2.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        elevation = 1.dp,
                        color = it.color,
                        modifier = modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                tagForDeletion.value = Optional.of(it)
                            }
                        )
                    ) {
                        Text(
                            text = cutText(it.caption, 40),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.overline,
                            modifier = Modifier.padding(4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun cutText(text: String, maxLength: Int): String {
    return if (text.length > maxLength) {
        ".${text.length}.${maxLength}.${text.substring(0, maxLength - 3)}..."
    } else {
        text
    }
}

//@Preview
//@ExperimentalFoundationApi
//@Composable
//fun ViewTagArea() {
//    GraphNotesTheme {
//        TagArea(
//            tags = setOf(DictionaryElement(caption = "test 1"), DictionaryElement(caption = "test 2"),
//                DictionaryElement(caption = "test 3", color = Color.Red), DictionaryElement(caption = "test 3", color = Color.Red),
//                DictionaryElement(caption = "test 3", color = Color.Red), DictionaryElement(caption = "test with very long long long long long log long long text"),
//                DictionaryElement(caption = "test 1111111"), DictionaryElement(caption = "test 22222222222")
//            )
//        )
//    }
//}