@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class)

package ru.vat78.notes.clients.android.ui.screens.editor.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.TmpIcons
import ru.vat78.notes.clients.android.ui.components.TagArea
import ru.vat78.notes.clients.android.ui.components.TextFieldWithAutocomplete
import ru.vat78.notes.clients.android.ui.screens.editor.NoteEditorUiState
import ru.vat78.notes.clients.android.ui.screens.editor.NotesEditorUiEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@Composable
fun NoteEditForm(
    uiState: NoteEditorUiState,
    sendEvent: (NotesEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    scrollableState: ScrollState = rememberScrollState()
) {
    val note = uiState.changed.note
    val noteType = uiState.noteType
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
                text = note.description,
                onChanges = {
                    sendEvent.invoke(NotesEditorUiEvent.ChangeEvent.ChangeDescription(it))
                },
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
                val multiTags = !noteType.hierarchical
                if (multiTags) {
                    TagArea(
                        tags = uiState.changed.parents,
                        onDeleteTag = {
                            sendEvent.invoke(NotesEditorUiEvent.RemoveTag(it))
                        },
                        modifier = modifier.fillMaxWidth()
                    )
                }

                val text = remember { mutableStateOf("") }
                TextFieldWithAutocomplete(
                    textSource = text::value,
                    hint = stringResource(R.string.tags_input_hint),
                    tipsProvider = uiState::suggestions,
                    tipsOnTop = multiTags,
                    onQuery =  {
                        sendEvent.invoke(NotesEditorUiEvent.RequestSuggestions(it))
                        text.value = it
                    },
                    onValueSet = {
                        sendEvent.invoke(NotesEditorUiEvent.AddTag(it))
                    },
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
                imageVector = TmpIcons[type.icon] ?: Icons.Filled.Note,
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
                                imageVector = TmpIcons[typeOption.icon] ?: Icons.Filled.Note,
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
    text: String,
    modifier: Modifier = Modifier,
    onChanges: (String) -> Unit = {}
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight(0.3f)
    ) {
        TextField(
            value = text,
            singleLine = false,
            onValueChange = {
                onChanges.invoke(it)
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
                .fillMaxHeight()
        )
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