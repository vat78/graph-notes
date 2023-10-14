package ru.vat78.notes.clients.android.data.room.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import ru.vat78.notes.clients.android.data.NoteLink
import java.time.Instant

@Immutable
@Entity(tableName = "links", primaryKeys = ["parent", "child"])
data class LinkEntity(
    val parent: String,
    val child: String,
    val deleted: Boolean = false,
    val lastUpdate: Long = Instant.now().epochSecond
) {
    constructor (link: NoteLink, cleanLastUpdate: Boolean = false): this (
        parent = link.parentId,
        child = link.childId,
        deleted = link.deleted,
        lastUpdate = if (cleanLastUpdate) 0 else Instant.now().epochSecond
    )

    fun toNoteLink() : NoteLink {
        return NoteLink(
            parentId = parent,
            childId = child,
            deleted = deleted,
            lastUpdate = lastUpdate
        )
    }
}
