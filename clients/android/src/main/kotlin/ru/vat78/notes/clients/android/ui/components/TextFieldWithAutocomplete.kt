@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

package ru.vat78.notes.clients.android.ui.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import ru.vat78.notes.clients.android.data.DictionaryElement

//@Composable
//fun TextFieldWithAutocomplete(
//    textSource: () -> String,
//    tipsProvider: () -> List<DictionaryElement>,
//    onQuery: (String) -> Unit,
//    onValueSet: (DictionaryElement) -> Unit,
//    modifier: Modifier = Modifier,
//    hint: String = "",
//    tipsOnTop: Boolean = false,
//) {
//
//    val itemList = tipsProvider.invoke()
//    val text = textSource.invoke()
//    val correctValue = itemList.find { it.caption == text }
//    val expanded = remember { mutableStateOf(false)}
//    val textFieldSize = remember { mutableStateOf(Size.Zero)}
//
//    println("------------ itemList: ${itemList.size} --- expanded: ${expanded.value} ")
//
//    Column(modifier = modifier.fillMaxWidth()) {
//        if (tipsOnTop) {
//            DropDownForAutocomplete(expanded, textFieldSize, itemList, text, onValueSet)
//            TextFieldForAutocomplete(text, onQuery, hint, correctValue, textFieldSize)
//        } else {
//            TextFieldForAutocomplete(text, onQuery, hint, correctValue, textFieldSize)
//            DropDownForAutocomplete(expanded, textFieldSize, itemList, text, onValueSet)
//        }
//    }
//}
//
//@Composable
//private fun DropDownForAutocomplete(
//    expanded: MutableState<Boolean>,
//    textFieldSize: MutableState<Size>,
//    itemList: List<DictionaryElement>,
//    text: String,
//    onValueSet: (DictionaryElement) -> Unit
//) {
//    AnimatedVisibility(visible = expanded.value) {
//        Card(
//            modifier = Modifier
//                .padding(horizontal = 5.dp)
//                .width(textFieldSize.value.width.dp),
//            elevation = 15.dp,
//            shape = RoundedCornerShape(10.dp)
//        ) {
//            LazyColumn(
//                modifier = Modifier.heightIn(max = 150.dp),
//            ) {
//                for (tip in itemList) {
//                    if (tip.caption != text) {
//                        item {
//                            Text(
//                                tip.caption,
//                                modifier = Modifier
//                                    .padding(4.dp)
//                                    .clickable(
//                                        onClick = {
//                                            onValueSet.invoke(tip)
//                                            expanded.value = false
//                                        }
//                                    )
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun TextFieldForAutocomplete(
    text: String,
    onQuery: (String) -> Unit,
    hint: String,
    correctValue: DictionaryElement?,
    textFieldSize: MutableState<Size>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = text,
        onValueChange = {
            onQuery.invoke(it)
        },
        maxLines = 1,
        placeholder = { Text(hint) },
        isError = correctValue == null && text.isNotEmpty(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                textFieldSize.value = coordinates.size.toSize()
            },
    )
}