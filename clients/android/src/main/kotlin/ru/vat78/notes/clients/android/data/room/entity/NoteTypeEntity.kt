package ru.vat78.notes.clients.android.data.room.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.vat78.notes.clients.android.data.NoteType
import ru.vat78.notes.clients.android.data.TimeDefault
import java.time.Instant


@Immutable
@Entity(
    tableName = "note_types"
)
data class NoteTypeEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val icon: String,
    val tag: Boolean,
    val hierarchical: Boolean,
    val symbol: String,
    val defaultStart: String,
    val defaultFinish: String,
    val isDefault: Boolean,
    val deleted: Boolean,
    val lastUpdate: Long
) {
    constructor (type: NoteType) : this(
        id = type.id,
        name = type.name,
        icon = type.icon,
        tag = type.tag,
        hierarchical = type.hierarchical,
        symbol = type.symbol + "",
        defaultStart = type.defaultStart.name,
        defaultFinish = type.defaultFinish.name,
        isDefault = type.default,
        deleted = false,
        lastUpdate = Instant.now().epochSecond
    )

    fun toNoteType(): NoteType {
        return NoteType(
            id = id,
            name = name,
            icon = icon,
            tag = tag,
            hierarchical = hierarchical,
            symbol = symbol[0],
            defaultStart = TimeDefault.valueOf(defaultStart),
            defaultFinish = TimeDefault.valueOf(defaultFinish),
            default = isDefault
        )
    }
}