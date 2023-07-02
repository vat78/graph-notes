package ru.vat78.notes.clients.android.ui.ext

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import ru.vat78.notes.clients.android.data.DictionaryElement

fun TextFieldValue.analyzeTags(tagMarkers: List<Char>, previousValue: String): Triple<String, IntRange, TextFieldValue> {
    Log.i("TextFieldValue", "Analyze text $text with selection $selection against old $previousValue")
    if (selection.start != selection.end) {
        Log.i("TextFieldValue", "There is a text selected")
        return Triple("", IntRange.EMPTY, this)
    }

    var tagStartSymbol: Char? = null
    var lastSpace = -1
    var selectedTag = -1
    val checkingText = if (previousValue.length > text.length) previousValue else text

    for (cursorPosition in 0 .. checkingText.lastIndex) {
        val symbol = checkingText[cursorPosition]
        if (cursorPosition < selection.start) {
            if (symbol.isWhitespace()) {
                lastSpace = cursorPosition
            } else {
                if (tagStartSymbol == null) {
                    if (tagMarkers.contains(symbol) && lastSpace == cursorPosition - 1) {
                        tagStartSymbol = symbol
                        selectedTag = cursorPosition
                    }
                } else {
                    if (symbol == tagStartSymbol) {
                        tagStartSymbol = null
                        selectedTag = -1
                    }
                }
            }
        } else {
            if (tagStartSymbol == null) {
                break
            } else if (symbol == tagStartSymbol) {
                Log.i("TextFieldValue", "Cursor at $selection is inside tag from $selectedTag to $cursorPosition")
                return Triple("", IntRange.EMPTY, this.copy(text = previousValue, selection = TextRange(selectedTag, cursorPosition + 1)))
            }
        }
    }

    val textBeforeCursor = if (lastSpace < 0 || lastSpace + 1 >= selection.start) "" else text.substring(lastSpace + 1, selection.start)
    val tagSuggestion = if (tagStartSymbol == null) IntRange.EMPTY else IntRange(selectedTag, selection.start - 1)
    return Triple(textBeforeCursor, tagSuggestion, this)
}

fun TextFieldValue.curTagPosition(tagMarkers: List<Char>): Int {
    if (selection.start != selection.end) {
        Log.i("TextFieldValue", "There is a text selected")
        return -1
    }
    var tagStart = -1
    var tagStartSymbol: Char? = null
    for (cursorPosition in 0 .. text.lastIndex) {
        if (cursorPosition >= selection.start) {
            break
        }
        val symbol = text[cursorPosition]
        if (tagStartSymbol == null) {
             if (tagMarkers.contains(symbol) && (cursorPosition ==0 || text[cursorPosition - 1].isWhitespace())) {
                tagStartSymbol = symbol
                 tagStart = cursorPosition
            }
        } else {
            if (symbol == tagStartSymbol) {
                tagStartSymbol = null
                tagStart = -1
            }
        }
    }
    return tagStart
}

fun TextFieldValue.insertSuggestedTag(tag: DictionaryElement, tagMarkers: List<Char>): TextFieldValue {
    val tagStartPosition = curTagPosition(tagMarkers)
    if (tagStartPosition < 0) {
        Log.w("TextFieldValue", "Selected tag, but it was not found in text")
        return this
    }
    val builder = StringBuilder()
    builder.append(text.substring(0, tagStartPosition))
        .append("#")
        .append(tag.caption)
        .append("# ")
    val newCursorPosition = builder.length
    builder.append(text.substring(selection.start))
    return copy(
        annotatedString = AnnotatedString(builder.toString()),
        selection = TextRange(newCursorPosition, newCursorPosition)
    )
}

fun TextFieldValue.highlightTags(tagMarkers: List<Char>, previousValue: AnnotatedString): TextFieldValue  {
    Log.i("TestInput", "New: ${annotatedString.text}, old: ${previousValue.text}, old spans ${previousValue.spanStyles} new - ${annotatedString.spanStyles}")
    if (previousValue.length < annotatedString.length) {
        return copy(annotatedString = formatFullText(annotatedString.text, tagMarkers))
    } else if (previousValue.length > annotatedString.length && previousValue.spanStyles.isNotEmpty()) {
        val cursorPosition = this.selection.end
        val affectedBlock = previousValue.spanStyles.firstOrNull{ it.start < cursorPosition && cursorPosition < it.end}
        Log.i("TestInput", "Affected block ${affectedBlock} because cursor at ${selection}")
        if (affectedBlock != null && tagMarkers.contains(previousValue.text[affectedBlock.end - 1])) {
            return copy(
                annotatedString = previousValue,
                selection = TextRange(affectedBlock.start, affectedBlock.end)
            )
        }
    }
    return copy(annotatedString = formatFullText(annotatedString.text, tagMarkers))
}

private fun formatFullText(text: String, tagMarkers: List<Char>) : AnnotatedString {
    return buildAnnotatedString {
        var processedPosition = -1
        var tagStartSymbol: Char? = null

        for (cursorPosition in 0 .. text.lastIndex) {
            val symbol = text[cursorPosition]
            if (tagStartSymbol != null && symbol == tagStartSymbol) {
                append(AnnotatedString(
                    text = text.slice(processedPosition + 1 .. cursorPosition),
                    spanStyle = SpanStyle(fontStyle = FontStyle.Italic)
                ))
                tagStartSymbol = null
                processedPosition = cursorPosition
            } else if (tagMarkers.contains(symbol) &&(cursorPosition == 0 || text[cursorPosition-1] == ' ' )) {
                append(text.slice(processedPosition + 1 until cursorPosition))
                tagStartSymbol = symbol
                processedPosition = cursorPosition - 1
            }
        }
        if (processedPosition < text.lastIndex) {
            if (tagStartSymbol == null) {
                append(text.slice(processedPosition + 1..text.lastIndex))
            } else {
                append(
                    AnnotatedString(
                        text = text.slice(processedPosition + 1..text.lastIndex),
                        spanStyle = SpanStyle(fontStyle = FontStyle.Italic)
                    )
                )
            }
        }
    }
}

fun String.insertTags(tags: Map<String, DictionaryElement>): String {
    val text = this
    return buildString {
        var lastProcessed = -1
        var curSymbol = 0
        while (curSymbol < text.length) {
            if (text[curSymbol] == '#' && (curSymbol == 0 || text[curSymbol - 1].isWhitespace())) {
                append(text.substring(lastProcessed + 1, curSymbol))
                lastProcessed = curSymbol
                do {
                    curSymbol++
                } while (curSymbol < text.length && text[curSymbol] != '#')
                if (curSymbol < text.length) {
                    val tagCaption = text.substring(lastProcessed + 1, curSymbol)
                    append("#{")
                    append(tags[tagCaption]?.id ?: "error")
                    append("}")
                    lastProcessed = curSymbol
                }
            }
            curSymbol++
        }
        append(text.substring(lastProcessed + 1, text.length))
    }
}
fun String.insertCaptions(tags: Map<String, DictionaryElement>): String {
    val text = this
    return buildString {
        var lastProcessed = -1
        var curSymbol = 0
        while (curSymbol < text.length) {
            if (text[curSymbol] == '#' && curSymbol + 1 < text.length && text[curSymbol+1] == '{') {
                append(text.substring(lastProcessed + 1, curSymbol))
                curSymbol++
                lastProcessed = curSymbol
                do {
                    curSymbol++
                } while (curSymbol < text.length && text[curSymbol] != '}')
                if (curSymbol < text.length) {
                    val tagId = text.substring(lastProcessed + 1, curSymbol)
                    append("#")
                    append(tags[tagId]?.caption ?: "error")
                    append("#")
                    lastProcessed = curSymbol
                }
            }
            curSymbol++
        }
        append(text.substring(lastProcessed + 1, text.length))
    }
}