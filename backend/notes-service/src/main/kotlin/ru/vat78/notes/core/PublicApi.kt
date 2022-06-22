package ru.vat78.notes.core.api

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import java.util.*
import javax.json.bind.annotation.JsonbCreator
import javax.json.bind.annotation.JsonbProperty

enum class NoteType(val tag: Boolean, val hierarchy: Boolean) {
    COMMON(false, false),
    DATE(false, false),
    USER(true, false),
    PERSON(true, false)
}

class Note @JsonbCreator constructor (
    @JsonbProperty("id") _id: String?,
    val type: NoteType,
    val caption: String,
    val text: String) {
    val id = _id ?: UUID.randomUUID().toString()

    override fun toString(): String {
        return "Note[$id]"
    }
}

interface NoteService {
    fun save(note: Note) : Uni<Note>
    fun findByCaption(caption: String): Multi<Note>
}



