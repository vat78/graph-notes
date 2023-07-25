package ru.vat78.notes.clients.android.data.room.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import java.time.Instant

@Immutable
@Entity(tableName = "links", primaryKeys = ["parent", "child"])
data class LinkEntity(
    val parent: String,
    val child: String,
    val deleted: Boolean = false,
    val lastUpdate: Long = Instant.now().epochSecond
)
