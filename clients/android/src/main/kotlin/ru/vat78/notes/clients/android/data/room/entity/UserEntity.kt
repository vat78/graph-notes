package ru.vat78.notes.clients.android.data.room.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "users"
)
data class UserEntity (
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val email: String
) {

}