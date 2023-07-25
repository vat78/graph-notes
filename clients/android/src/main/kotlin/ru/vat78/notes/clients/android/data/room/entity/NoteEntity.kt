package ru.vat78.notes.clients.android.data.room.entity

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.vat78.notes.clients.android.data.DictionaryElement
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.NoteType
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.HashMap

@Immutable
@Entity(
    tableName = "notes"
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val typeId: String,
    val caption: String?,
    val color: Long,
    val description: String?,
    val start: Long,
    val finish: Long,
    val root: Boolean = false,
    val suggestions: String,
    val deleted: Boolean,
    val lastUpdate: Long
) {

    constructor (note: Note) : this (
        id = note.id,
        typeId = note.type.id,
        caption = note.caption.ifBlank { null },
        color = note.color.value.toLong(),
        description = note.description.ifBlank { null },
        start = note.start.toEpochSecond(),
        finish = note.finish.toEpochSecond(),
        root = note.root,
        deleted = false,
        lastUpdate = Instant.now().epochSecond,
        suggestions = note.textInsertions.entries.map { "${it.key}|${it.value.caption}" }.joinToString("|")
    )

    fun toNote(types: Map<String, NoteType>) : Note {
        return Note(
            id = id,
            type = types[typeId]!!,
            caption = caption ?: "",
            description = description ?: "",
            color = Color(color),
            start = ZonedDateTime.ofInstant(Instant.ofEpochSecond(start), TimeZone.getDefault().toZoneId()),
            finish = ZonedDateTime.ofInstant(Instant.ofEpochSecond(finish), TimeZone.getDefault().toZoneId()),
            root = root,
            textInsertions = parseSuggestions(suggestions),
        )
    }

    fun toDictionary(types: Map<String, NoteType>) : DictionaryElement {
        return DictionaryElement(
            id = id,
            type = types[typeId]!!,
            caption = caption ?: "",
            color = Color(color)
        )
    }

    private fun parseSuggestions(suggestions: String): Map<String, DictionaryElement> {
        val list = suggestions.split("|")
        if (list.size == 1) {
            return emptyMap()
        }
        val result = HashMap<String, DictionaryElement>(list.size / 2)
        for(i in list.indices step 2) {
            result[list[i]] = DictionaryElement(id =list[i], type = NoteType(), caption = list[i+1])
        }
        return result
    }
}