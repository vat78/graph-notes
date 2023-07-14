package ru.vat78.notes.clients.android.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.ui.TagNotesScreen
import ru.vat78.notes.clients.android.ui.components.SuggestionList
import ru.vat78.notes.clients.android.ui.screens.editor.views.NoteEditForm

@ExperimentalMaterial3Api
@Composable
fun NoteEditor(
    appState: AppState,
    modifier: Modifier = Modifier,
    noteUuid: String,
    onExit: () -> Unit = { }
) {
    val viewModel = viewModel { NoteEditorViewModel(appState) }
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
                title = { Text(text = stringResource(R.string.save_alert_title)) },
                text = { Text(text = stringResource(R.string.save_alert_text)) },
                confirmButton = {
                    Text(
                        text = stringResource(R.string.save_alert_confirm_button),
                        modifier = Modifier.clickable {
                            openExitDialog.value = false
                            viewModel.sendEvent(NotesEditorUiEvent.CancelChanges)
                        }
                    ) },
                dismissButton = {
                    Text(
                        text = stringResource(R.string.save_alert_cancel_button),
                        modifier = Modifier.clickable { openExitDialog.value = false }
                    )
                }
            )
        }
        Box(Modifier.fillMaxSize()) {
            NoteEditForm(
                uiState = uiState,
                sendEvent = viewModel::sendEvent,
                tagTypes = viewModel.noteTypes.values.filter { it.tag && !it.hierarchical },
                modifier = Modifier.padding(it),
                onTagClick = {id -> appState.navigate("${TagNotesScreen.route}/$id")}
            )
            SuggestionList(
                suggestions = uiState.suggestions,
                modifier = Modifier.align(Alignment.BottomCenter),
                onSelectSuggestion = { viewModel.sendEvent(NotesEditorUiEvent.AddTag(it)) }
            )
        }

    }
}
