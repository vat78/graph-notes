package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.defaultTypes
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@Composable
fun SuggestionList(
    suggestions: List<DictionaryElement>,
    modifier: Modifier = Modifier,
    onSelectSuggestion: (DictionaryElement) -> Unit = {},
) {
    if (suggestions.isNotEmpty()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp,
            modifier = modifier
        ) {
            LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                itemsIndexed(suggestions) { index, item ->
                    val text = if (suggestions[index].id.isBlank())
                        stringResource(R.string.new_tag_prefix) + suggestions[index].caption
                    else suggestions[index].caption
                    TagRecord(
                        text = text,
                        iconName = suggestions[index].type.icon,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            .clickable { onSelectSuggestion.invoke(suggestions[index]) }
                    )
                    if (index < suggestions.lastIndex)
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview
@Composable
fun SuggestionListPreview() {
    GraphNotesTheme {
        SuggestionList(
            suggestions = listOf(DictionaryElement("", defaultTypes.get(0), "Test note for suggestion"),
                DictionaryElement("", defaultTypes.get(0), "Test note for suggestion with very very very very very long text")),
            modifier = Modifier.fillMaxWidth()
        )
    }
}