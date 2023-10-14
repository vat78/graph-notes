package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.ui.screens.editor.views.TypeAndCaption

@Composable
fun NewTagAlert(
    tag: DictionaryElement,
    tagTypes: Collection<NoteType>,
    error: String?,
    onConfirm: (DictionaryElement) -> Unit = { },
    onDismiss: () -> Unit = { },
    onChangeType: (NoteType) -> Unit = { },
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.new_tag_alert_title)) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                TypeAndCaption(
                    type = tag.type,
                    caption = tag.caption,
                    availableTypes = tagTypes,
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    onTypeChanges = onChangeType
                )
                if (error!= null) {
                    Text(text = "${R.string.cant_save_this} $error")
                }}
               },
        confirmButton = {
            Text(
                text = stringResource(R.string.new_tag_confirm_button),
                modifier = Modifier.clickable {
                    onConfirm.invoke(tag)
                }
            ) },
        dismissButton = {
            Text(
                text = stringResource(R.string.new_tag_cancel_button),
                modifier = Modifier.clickable { onDismiss.invoke() }
            )
        }
    )
}