package ru.vat78.notes.clients.android.data.room.entity

import androidx.room.Entity

@Entity(tableName = "suggestions", primaryKeys = ["wordId", "tagId"])
data class SuggestionEntity(
    val wordId: Long,
    val tagId: String,
    val typeId: String
)

