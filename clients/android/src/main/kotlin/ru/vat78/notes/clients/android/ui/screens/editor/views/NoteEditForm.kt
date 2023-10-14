@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class)

package ru.vat78.notes.clients.android.ui.screens.editor.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteWithParents
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.data.getIcon
import ru.vat78.notes.clients.android.ui.components.NewTagAlert
import ru.vat78.notes.clients.android.ui.components.SymbolAnnotationType
import ru.vat78.notes.clients.android.ui.components.TagArea
import ru.vat78.notes.clients.android.ui.components.TextFieldForAutocomplete
import ru.vat78.notes.clients.android.ui.components.messageFormatter
import ru.vat78.notes.clients.android.ui.screens.editor.DescriptionFocusState
import ru.vat78.notes.clients.android.ui.screens.editor.EditFormState
import ru.vat78.notes.clients.android.ui.screens.editor.NoteEditorUiState
import ru.vat78.notes.clients.android.ui.screens.editor.NotesEditorUiEvent
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@Composable
fun NoteEditForm(
    uiState: NoteEditorUiState,
    sendEvent: (NotesEditorUiEvent) -> Unit,
    tagTypes: Collection<NoteType>,
    modifier: Modifier = Modifier,
    scrollableState: ScrollState = rememberScrollState(),
    onTagClick: (String) -> Unit = { }
) {
    val note = uiState.changed.note
    val noteType = uiState.noteType

    if (uiState.newTag != null) {
        val newTag = uiState.newTag
        NewTagAlert(
            tag = newTag,
            error = null,
            tagTypes = tagTypes,
            onDismiss = { sendEvent.invoke(NotesEditorUiEvent.CancelNewTag) },
            onConfirm = { sendEvent.invoke(NotesEditorUiEvent.CreateNewTag(it)) },
            onChangeType = { sendEvent.invoke(NotesEditorUiEvent.ChangeNewTagType(newTag, it)) }
        )
    }

    Surface (
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.verticalScroll(scrollableState)) {
            TypeAndCaption(
                type = noteType,
                caption = note.caption,
                availableTypes = uiState.availableTypes,
                onTypeChanges = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeEvent.ChangeType(it))
                },
                onCaptionChanges = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeEvent.ChangeCaption(it))
                }
            )

            DescriptionEditor(
                textFieldValue = uiState.descriptionTextValue,
                note = uiState.changed,
                editFormFocused = uiState.descriptionFocus,
                onChanges = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeEvent.ChangeDescription(it))
                },
                onDescriptionFocus = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeDescriptionFocus(it))
                },
                onTagClick = onTagClick,
                modifier = Modifier.fillMaxWidth()
            )
            TimeEditors(
                note = note,
                onStartChanged = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeEvent.ChangeStart(it))
                },
                onFinishChanged = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeEvent.ChangeFinish(it))
                }
            )



            Column(modifier = Modifier.fillMaxWidth()) {
                TagArea(
                    tags = uiState.changed.parents,
                    onClickTag = {
                        onTagClick.invoke(it.id)
                    },
                    onDeleteTag = {
                        sendEvent.invoke(NotesEditorUiEvent.RemoveTag(it))
                    },
                    modifier = modifier.fillMaxWidth()
                )

                val text = remember { mutableStateOf("") }
                val textFieldSize = remember { mutableStateOf(Size.Zero)}
                TextFieldForAutocomplete (
                    text = text.value,
                    hint = stringResource(R.string.tags_input_hint),
                    onQuery =  {
                        sendEvent.invoke(NotesEditorUiEvent.RequestSuggestions(it))
                        text.value = it
                    },
                    correctValue = null,
                    textFieldSize = textFieldSize
                )
            }


        }
    }
}


@Composable
fun TypeAndCaption(
    type: NoteType,
    caption: String,
    availableTypes: Collection<NoteType>,
    modifier: Modifier = Modifier,
    onTypeChanges: (NoteType) -> Unit = {},
    onCaptionChanges: (String) -> Unit = {},
) {
    val expanded = remember { mutableStateOf(false) }

    Row(modifier = modifier.padding(8.dp).fillMaxWidth()) {

        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(
                imageVector = getIcon(type),
                contentDescription = type.name,
                modifier = Modifier
                    .background(Color.Transparent)
                    .clickable { expanded.value = !expanded.value }
                    .padding(8.dp)
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                for (typeOption in availableTypes) {
                    DropdownMenuItem(
                        onClick = {
                            onTypeChanges.invoke(typeOption)
                            expanded.value = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getIcon(typeOption),
                                contentDescription = typeOption.name,
                            )
                        },
                        text = {
                            Text(text = typeOption.name)
                        }
                    )
                }
            }
        }

        if (type.tag) {
            TextField(
                value = caption,
                singleLine = false,
                maxLines = 2,
                onValueChange = {
                    onCaptionChanges.invoke(it)
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    containerColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = LocalContentColor.current,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth()
            )
        }

    }
}

@Composable
fun DescriptionEditor(
    textFieldValue: TextFieldValue,
    note: NoteWithParents,
    editFormFocused: DescriptionFocusState,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.None),
    onChanges: (TextFieldValue) -> Unit = {},
    onDescriptionFocus: (DescriptionFocusState) -> Unit = {},
    onTagClick: (String) -> Unit = { }
) {
    Card(
        modifier = modifier
    ) {
        val focusRequester = remember { FocusRequester() }
        if (editFormFocused != DescriptionFocusState.HIDE) {
            val focusManager = LocalFocusManager.current
            val keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            BasicTextField(
                value = textFieldValue,
                onValueChange = { onChanges.invoke(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onDescriptionFocus.invoke(DescriptionFocusState.FOCUSED)
                        } else {
                            onDescriptionFocus.invoke(DescriptionFocusState.HIDE)
                        }
                    },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                maxLines = 10,
                cursorBrush = SolidColor(LocalContentColor.current),
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                onTextLayout = { focusRequester.requestFocus() }
            )
        } else {
            val uriHandler = LocalUriHandler.current
            val styledMessage = messageFormatter(note.note.description, note.note.textInsertions)
            ClickableText(
                text = styledMessage,
                style = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                onClick = {
                    val annotation = styledMessage
                        .getStringAnnotations(start = it, end = it)
                        .firstOrNull()
                    if (annotation == null) {
                        onDescriptionFocus.invoke(DescriptionFocusState.SHOW)
                    } else {
                        when (annotation.tag) {
                            SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                            SymbolAnnotationType.TAG.name -> onTagClick.invoke(annotation.item)
                            else -> {
                                onDescriptionFocus.invoke(DescriptionFocusState.SHOW)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TimeEditors(
    note: Note,
    onStartChanged: (ZonedDateTime) -> Unit = {},
    onFinishChanged: (ZonedDateTime) -> Unit = {},
    onAlignStart: () -> Unit = {},
) {
    val mContext = LocalContext.current

    val startTimePicker = TimePickerDialog(
        mContext,
        {_, mHour : Int, mMinute: Int ->
            onStartChanged.invoke(note.start.withHour(mHour).withMinute(mMinute))
        },
        note.start.hour, note.start.minute, true
    )
    val finishTimePicker = TimePickerDialog(
        mContext,
        {_, mHour : Int, mMinute: Int ->
            onFinishChanged.invoke(note.finish.withHour(mHour).withMinute(mMinute))
        },
        note.finish.hour, note.finish.minute, true
    )
    val startDatePicker = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            onStartChanged.invoke(note.start.withYear(mYear).withMonth(mMonth+1).withDayOfMonth(mDayOfMonth))
        },
        note.start.year, note.start.monthValue, note.start.dayOfMonth
    )
    val finishDatePicker = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            onFinishChanged.invoke(note.start.withYear(mYear).withMonth(mMonth+1).withDayOfMonth(mDayOfMonth))
        },
        note.finish.year, note.finish.monthValue, note.finish.dayOfMonth
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(0.5f)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Start", modifier = Modifier.align(Alignment.CenterHorizontally))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = note.start.format(DateTimeFormatter.ofPattern("HH:mm")),
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(0.35f)
                            .clickable { startTimePicker.show() }
                    )
                    Text(
                        text = note.start.format(DateTimeFormatter.ofPattern("dd-MM-yy")),
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable { startDatePicker.show() }
                    )
                }
                if (ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).isBefore(note.start)) {
                    Text(
                        text = "Set to last time today",
                        modifier = Modifier.clickable { onAlignStart.invoke() }.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Finish", modifier = Modifier.align(Alignment.CenterHorizontally))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = note.finish.format(DateTimeFormatter.ofPattern("HH:mm")),
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(0.35f)
                            .clickable { finishTimePicker.show() }
                    )
                    Text(
                        text = note.finish.format(DateTimeFormatter.ofPattern("dd-MM-yy")),
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable { finishDatePicker.show() }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun NoteEditFormPreview() {
    val types = runBlocking { StubAppContext().noteTypeStorage.getTypes() }.toList()
    val note = Note(
        type = types.find { !it.tag }!!,
        caption = "Should not be visible",
        description = "Some test text. Some test text. Some test text. Some test text. "
    )

    GraphNotesTheme {
        NoteEditForm(
            uiState = NoteEditorUiState(
                origin = NoteWithParents(note, emptySet()),
                changed = NoteWithParents(note, emptySet()),
                noteType = note.type,
                availableTypes = types,
                status = EditFormState.NEW,
                descriptionFocus = DescriptionFocusState.SHOW,
                descriptionTextValue = TextFieldValue(note.description)
            ),
            sendEvent = {},
            tagTypes = emptyList()
        )
    }
}