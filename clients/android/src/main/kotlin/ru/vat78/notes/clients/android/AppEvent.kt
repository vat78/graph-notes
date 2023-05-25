package ru.vat78.notes.clients.android

import ru.vat78.notes.clients.android.data.User

sealed class AppEvent {

    data class OnAuth(
        val user: User?
    ) : AppEvent()

    data class InitUser(
        val user: User?
    ) : AppEvent()
}