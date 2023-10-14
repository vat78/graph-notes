package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable


//@Immutable
//data class NoteWithChildren(
//    val note: Note,
//    val children: List<String> = emptyList(),
//)

@Immutable
data class User(
    val id: String,
    val name: String,
    val email: String
)



