package ru.vat78.notes.clients.android.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NotesStorage
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditor(
    viewModel: NoteEditorViewModel,
    modifier: Modifier = Modifier,
    noteUuid: String,
    onExit: () -> Unit = { }
) {
    viewModel.sendEvent(NotesEditorUiEvent.LoadNote(noteUuid))

    val uiState by viewModel.state.collectAsState()
    val openExitDialog = remember { mutableStateOf(false) }

    if (EditFormState.CLOSED == uiState.state) {
        onExit.invoke()
    }
    BackHandler(enabled = (EditFormState.CHANGED == uiState.state), onBack = {openExitDialog.value = true})

    Scaffold(
        modifier = modifier.padding(vertical = 16.dp),
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
            note = uiState.note,
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
fun EditNoteForm(
    note: Note,
    modifier: Modifier = Modifier,
) {
    Surface (modifier = modifier) {
        Column {
            Text(text = "Note editor")
            Text(text = "Note type: ${note.type}")
            Text(text = "Note caption: ${note.caption}")
            Text(text = "Note description: ${note.description}")
        }
    }
}

@Preview
@Composable
fun NoteEditorPreview() {
    NoteEditor(
        viewModel = NoteEditorViewModel(NotesStorage()),
        noteUuid = "new"
    )
}