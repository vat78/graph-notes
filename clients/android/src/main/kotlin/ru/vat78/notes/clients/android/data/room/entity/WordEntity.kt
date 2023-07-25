package ru.vat78.notes.clients.android.data.room.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "words",
    indices = [Index(value = ["word"], unique = true)]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val wordId: Long = 0,
    val word: String
)
