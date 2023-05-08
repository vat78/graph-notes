@file:OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class,ExperimentalComposeUiApi::class)

package ru.vat78.notes.clients.android.notes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.NoteTypeStructure
import ru.vat78.notes.clients.android.data.NotesStorage
import ru.vat78.notes.clients.android.ui.components.TagArea
import ru.vat78.notes.clients.android.ui.components.TextFieldWithAutocomplete
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NoteEditor(
    viewModel: NoteEditorViewModel,
    modifier: Modifier = Modifier,
    noteUuid: String,
    onExit: () -> Unit = { }
) {
    val uiState by viewModel.state.collectAsState()
    val openExitDialog = remember { mutableStateOf(false) }

    if (EditFormState.NEW == uiState.status) {
        viewModel.sendEvent(NotesEditorUiEvent.LoadNote(noteUuid))
    }
    if (EditFormState.CLOSED == uiState.status) {
        onExit.invoke()
        viewModel.sendEvent(NotesEditorUiEvent.ResetState(noteUuid))
    }
    BackHandler(enabled = (EditFormState.CHANGED == uiState.status), onBack = {openExitDialog.value = true})

    Scaffold(
        modifier = modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.sendEvent(NotesEditorUiEvent.SaveNote(noteUuid == "new"))
                }
            ) {
                Text(text = "Save")
            }
        }
    ) {
        if (openExitDialog.value) {
            AlertDialog(
                onDismissRequest = { openExitDialog.value = false },
                title = { Text(text = "Exit without saving") },
                text = { Text(text = "All changes will be lost if you press Exit") },
                confirmButton = {
                    Text(
                        text = "Exit",
                        modifier = Modifier.clickable {
                            openExitDialog.value = false
                            viewModel.sendEvent(NotesEditorUiEvent.CancelChanges(""))
                        }
                    ) },
                dismissButton = { Text( text = "Cancel", modifier = Modifier.clickable { openExitDialog.value = false })}
            )
        }
        EditNoteForm(
            uiState = uiState,
            viewModel = viewModel,
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
fun EditNoteForm(
    uiState: NoteEditorUiState,
    viewModel: NoteEditorViewModel,
    modifier: Modifier = Modifier,
    scrollableState: ScrollState = rememberScrollState()
) {
    val note = uiState.note
    Surface (
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.verticalScroll(scrollableState)) {
            TypeAndCaption(
                type = note.type,
                caption = note.caption,
                onTypeChanges = {
                    viewModel.sendEvent(NotesEditorUiEvent.ChangeType(it))
                },
                onCaptionChanges = {
                    viewModel.sendEvent(NotesEditorUiEvent.ChangeCaption(it))
                }
            )

            if (note.type.description) {
                DescriptionEditor(
                    text = note.description,
                    onChanges = {
                        viewModel.sendEvent(NotesEditorUiEvent.ChangeDescription(it))
                    },
                )
            }
            if (note.type.time) {
                TimeEditors(
                    note = note,
                    onStartChanged = {
                        viewModel.sendEvent(NotesEditorUiEvent.ChangeStart(it))
                    },
                    onFinishChanged = {
                        viewModel.sendEvent(NotesEditorUiEvent.ChangeFinish(it))
                    }
                )
            }
            if (note.type.structure != NoteTypeStructure.NONE) {
                println("!!!!!!! Rebuild model for autocomplete !!!!!!!!!!")
                val autocompleteModel = if (note.type.structure == NoteTypeStructure.HIERARCHY)
                    viewModel.getModelForAutocomplete(
                        initialText = uiState.parentValue?.caption ?: "",
                        typeFilter = note.type,
                        timeFilter = note.type == NoteType.TASK)
                else
                    viewModel.getModelForAutocomplete(
                        initialText = "",
                        typeFilter = if (note.type.structure == NoteTypeStructure.ANY_TAGS) null else note.type,
                        timeFilter = false
                    )

                Column(modifier = Modifier.fillMaxWidth()) {
                    val multiTags = note.type.structure != NoteTypeStructure.HIERARCHY
                    if (multiTags) {
                        TagArea(
                            tags = uiState.tags,
                            onDeleteTag = {
                                viewModel.sendEvent(NotesEditorUiEvent.RemoveTag(it))
                            },
                            modifier = modifier.fillMaxWidth()
                        )
                    }

                    TextFieldWithAutocomplete(
                        textSource = autocompleteModel::text,
                        hint = "Enter tag name",
                        tipsProvider = autocompleteModel::suggestions,
                        tipsOnTop = multiTags,
                        onQuery = autocompleteModel::requestSuggestions,
                        onValueSet = {
                            viewModel.sendEvent(NotesEditorUiEvent.AddTag(it, !multiTags))
                        },
                    )
                }

            }
        }
    }
}

@Composable
fun TypeAndCaption(
    type: NoteType,
    caption: String,
    modifier: Modifier = Modifier,
    onTypeChanges: (NoteType) -> Unit = {},
    onCaptionChanges: (String) -> Unit = {},
) {
    val expanded = remember { mutableStateOf(false) }

    Row(modifier = modifier.padding(8.dp).fillMaxWidth()) {

        Box(modifier = Modifier.align(CenterVertically)) {
            Icon(
                imageVector = type.icon,
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
                for (typeOption in NoteType.values()) {
                    DropdownMenuItem(
                        onClick = {
                            onTypeChanges.invoke(typeOption)
                            expanded.value = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = typeOption.icon,
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

        if (type.caption) {
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
    onStartChanged: (LocalDateTime) -> Unit = {},
    onFinishChanged: (LocalDateTime) -> Unit = {},
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
            onStartChanged.invoke(note.start.withYear(mYear).withMonth(mMonth).withDayOfMonth(mDayOfMonth))
        },
        note.start.year, note.start.monthValue, note.start.dayOfMonth
    )
    val finishDatePicker = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            onFinishChanged.invoke(note.start.withYear(mYear).withMonth(mMonth).withDayOfMonth(mDayOfMonth))
        },
        note.finish.year, note.finish.monthValue, note.finish.dayOfMonth
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(0.5f)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Start", modifier = Modifier.align(CenterHorizontally))
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
                if (LocalDate.now().atStartOfDay().isBefore(note.start)) {
                    Text(
                        text = "Set to last time today",
                        modifier = Modifier.clickable { onAlignStart.invoke() }.align(CenterHorizontally)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Finish", modifier = Modifier.align(CenterHorizontally))
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

@Preview
@Composable
fun NoteEditorPreview() {
    GraphNotesTheme {
        NoteEditor(
            viewModel = NoteEditorViewModel(NotesStorage()),
            noteUuid = "test-uuid"
        )
    }
}