/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.vat78.notes.clients.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme
import java.util.UUID

// Regex containing the syntax tokens
val symbolPattern by lazy {
    Regex("""(https?://[^\s\t\n]+)|(`[^`]+`)|(#\{[\w\-]+\})|(\*[^*]+\*)|(_[^_]+_)|(~[^~]+~)""".trimMargin())
}

// Accepted annotations for the ClickableTextWrapper
enum class SymbolAnnotationType {
    TAG, LINK
}
typealias StringAnnotation = AnnotatedString.Range<String>
// Pair returning styled content and annotation for ClickableText when matching syntax token
typealias SymbolAnnotation = Pair<AnnotatedString, StringAnnotation?>

/**
 * Format a message following Markdown-lite syntax
 * | #{id} -> bold, primary color and clickable element
 * | http(s)://... -> clickable link, opening it into the browser
 * | *bold* -> bold
 * | _italic_ -> italic
 * | ~strikethrough~ -> strikethrough
 * | `MyClass.myMethod` -> inline code styling
 *
 * @param text contains message to be parsed
 * @return AnnotatedString with annotations used inside the ClickableText wrapper
 */
@Composable
fun messageFormatter(
    text: String,
    tags: Map<String, DictionaryElement>,
): AnnotatedString {
    val tokens = symbolPattern.findAll(text)

    return buildAnnotatedString {

        var cursorPosition = 0
        var newTextPosition = 0

        val codeSnippetBackground = MaterialTheme.colorScheme.surface

        for (token in tokens) {
            append(text.slice(cursorPosition until token.range.first))
            newTextPosition += (token.range.first - cursorPosition + 1)

            val (annotatedString, stringAnnotation) = getSymbolAnnotation(
                matchResult = token,
                tags = tags,
                colorScheme = MaterialTheme.colorScheme,
                codeSnippetBackground = codeSnippetBackground
            )
            append(annotatedString)

            if (stringAnnotation != null) {
                val (item, start, end, tag) = stringAnnotation
                addStringAnnotation(tag = tag, start = start + newTextPosition, end = end + newTextPosition, annotation = item)
            }

            cursorPosition = token.range.last + 1
            newTextPosition += annotatedString.length
        }

        if (!tokens.none()) {
            append(text.slice(cursorPosition..text.lastIndex))
        } else {
            append(text)
        }
    }
}

/**
 * Map regex matches found in a message with supported syntax symbols
 *
 * @param matchResult is a regex result matching our syntax symbols
 * @return pair of AnnotatedString with annotation (optional) used inside the ClickableText wrapper
 */
private fun getSymbolAnnotation(
    matchResult: MatchResult,
    tags: Map<String, DictionaryElement>,
    colorScheme: ColorScheme,
    codeSnippetBackground: Color
): SymbolAnnotation {
    val symbol = matchResult.value.first()
    return when (symbol) {
        '*' -> SymbolAnnotation(
            AnnotatedString(
                text = matchResult.value.trim('*'),
                spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
            ),
            null
        )
        '_' -> SymbolAnnotation(
            AnnotatedString(
                text = matchResult.value.trim('_'),
                spanStyle = SpanStyle(fontStyle = FontStyle.Italic)
            ),
            null
        )
        '~' -> SymbolAnnotation(
            AnnotatedString(
                text = matchResult.value.trim('~'),
                spanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
            ),
            null
        )
        '`' -> SymbolAnnotation(
            AnnotatedString(
                text = matchResult.value.trim('`'),
                spanStyle = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    background = codeSnippetBackground,
                    baselineShift = BaselineShift(0.2f)
                )
            ),
            null
        )
        'h' -> SymbolAnnotation(
            AnnotatedString(
                text = matchResult.value,
                spanStyle = SpanStyle(
                    color = colorScheme.primary
                )
            ),
            StringAnnotation(
                item = matchResult.value,
                start = 0,
                end = matchResult.value.length,
                tag = SymbolAnnotationType.LINK.name
            )
        )
        '#' -> buildTagLink(matchResult, tags, colorScheme.primary)
        else -> SymbolAnnotation(AnnotatedString(matchResult.value), null)
    }
}

private fun buildTagLink(matchResult: MatchResult, tags: Map<String, DictionaryElement>, color: Color) : SymbolAnnotation {
    val text = matchResult.value
    val id = text.substring(2, text.length - 1)
    val newText = tags[id]?.caption ?: "Undefined tag"
    return SymbolAnnotation(
        AnnotatedString(
            text = newText,
            spanStyle = SpanStyle(
                color = color,
                fontWeight = FontWeight.Bold
            )
        ),
        StringAnnotation(
            item = id,
            start = 0,
            end = newText.length,
            tag = SymbolAnnotationType.TAG.name
        )
    )
}


@Preview
@Composable
fun MessageFormatterPreview() {
    val uniqueId = UUID.randomUUID().toString()
    val types = runBlocking { StubAppContext().noteTypeStorage.getTypes() }.toList()
    val tags = mapOf(
        Pair("467c41ed-67fa-4a7d-82e0-6d2fa87459ac", DictionaryElement("467c41ed-67fa-4a7d-82e0-6d2fa87459ac", types[0], "test tag 1")),
        Pair(uniqueId, DictionaryElement(uniqueId, types[1], "test tag 2"))
    )
    GraphNotesTheme {
        val styledMessage = messageFormatter("""
            This is a test message with http://google.com and https://google.com links.
            With *bold text - and symbols* in the middle.
            With ~italic text - and symbols~ in the middle.
            With _underline text - and symbols_ in the middle.
            With first tag - #{467c41ed-67fa-4a7d-82e0-6d2fa87459ac}.
            With second tag - #{${uniqueId}}.
            With #persons with spaces and - symbols@.
            With &tasks with spaces and - symbols&.
            With phones +3323424423-234324-234 haha+.
        """.trimIndent(), tags)
        Surface(color = MaterialTheme.colorScheme.surface) {
            ClickableText(
                text = styledMessage,
                maxLines = 10,
                style = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                onClick = {}
            )
        }
    }
}