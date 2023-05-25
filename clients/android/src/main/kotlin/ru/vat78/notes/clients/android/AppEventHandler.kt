package ru.vat78.notes.clients.android

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppEventHandler(
    val context: ApplicationContext,
    val coroutineScope: CoroutineScope
) {

    fun riseEvent(event: AppEvent) {

        when (event) {
            is AppEvent.OnAuth -> {
                coroutineScope.launch {
                    context.setUser(event.user)
                    Log.i("Auth event", "User ${event.user?.name} authenticated")
                }
            }

            is AppEvent.InitUser -> {
                coroutineScope.launch {
                    Log.i("Auth event", "Init context for user ${event.user?.name}")
                    context.services.userStorage.saveUser(event.user)
                    context.services.noteTypeStorage.reload()
                    Log.i("Auth event", "All action for user ${event.user?.name} are triggered")
                }
            }
        }
    }
}