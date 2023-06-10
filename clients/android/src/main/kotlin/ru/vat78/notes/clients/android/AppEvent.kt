package ru.vat78.notes.clients.android

import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.data.User

sealed class AppEvent {

    data class OnAuth(
        val user: User?
    ) : AppEvent()

    data class NoteSaved(
        val previousVersion: Note,
        val newValue: Note
    ) : AppEvent()
}