package ru.vat78.notes.clients.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.data.TmpIcons
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.time.format.DateTimeFormatter

@Composable
fun NoteComponent(
    note: Note,
    noteType: NoteType?,
    color: Color = MaterialTheme.colorScheme.tertiary,
    onNoteClick: (Note) -> Unit = { },
    onTagClick: (String) -> Unit = { }
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(note.description, note.textInsertions)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
            modifier = Modifier.fillMaxWidth().clickable { onNoteClick(note) }
        ) {
            Column {
                Row (modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = TmpIcons[noteType?.icon] ?: Icons.Filled.Note,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp)
                            .height(18.dp)
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = note.caption,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = LocalContentColor.current,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                        Surface (
                            color = color,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = note.finish.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LocalContentColor.current,
                                    fontStyle = FontStyle.Italic
                                ),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                if (note.description.isNotBlank()) {
                    ClickableText(
                        text = styledMessage,
                        maxLines = 4,
                        style = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                        onClick = {
                            styledMessage
                                .getStringAnnotations(start = it, end = it)
                                .firstOrNull()
                                ?.let { annotation ->
                                    when (annotation.tag) {
                                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                                        SymbolAnnotationType.TAG.name -> onTagClick.invoke(annotation.item)
                                        else -> Unit
                                    }
                                }
                        }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Preview()
@Composable
fun NoteComponentPreview() {
    GraphNotesTheme {
        NoteComponent(
            note = StubAppContext().loadNotes(null)[0],
            noteType = null
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
fun NoteComponentPreviewDark() {
    GraphNotesTheme {
        GraphNotesTheme {
            NoteComponent(
                note = StubAppContext().loadNotes(null)[0],
                noteType = null
            )
        }
    }
}