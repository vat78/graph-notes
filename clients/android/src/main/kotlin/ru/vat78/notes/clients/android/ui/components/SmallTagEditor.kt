package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmallTagEditor(
    onEventInput: (String) -> Unit,
    onTextInput: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {}
) {
    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    Surface(tonalElevation = 2.dp) {
        Row(modifier = modifier) {
            Box(modifier = Modifier.fillMaxWidth(0.75f)) {
                UserInputText(
                    textFieldValue = textState,
                    onTextChanged = { onTextInput.invoke(it); textState = it },
                    // Only show the keyboard if there's no input selector and text field has focus
                    keyboardShown = textFieldFocusState,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onEventInput.invoke(textState.text)
                        textState = TextFieldValue("")
                        textFieldFocusState = false
                    }),
                    // Close extended selector if text field receives focus
                    onTextFieldFocused = { focused ->
                        if (focused) {
                            resetScroll()
                        }
                        textFieldFocusState = focused
                    },
                    focusState = textFieldFocusState
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // Send button
            val createNoteEnabled = textState.text.isNotBlank()
            val border = if (!createNoteEnabled) {
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            } else {
                null
            }
            val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            val buttonColors = ButtonDefaults.buttonColors(
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor
            )
            Button(
                modifier = Modifier.height(36.dp).align(Alignment.CenterVertically).padding(end = 16.dp),
                enabled = createNoteEnabled,
                onClick = {
                    onEventInput.invoke(textState.text)
                    textState = TextFieldValue("")
                    textFieldFocusState = false
                },
                colors = buttonColors,
                border = border,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    stringResource(id = R.string.add),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun SmallTagEditorPreview() {
    GraphNotesTheme {
        SmallTagEditor(onEventInput = {}, onTextInput = {})
    }
}