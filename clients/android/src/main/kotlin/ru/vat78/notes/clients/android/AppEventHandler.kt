package ru.vat78.notes.clients.android

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppEventHandler(
    private val context: ApplicationContext,
    private val coroutineScope: CoroutineScope
) {

    fun riseEvent(event: AppEvent) {

        when (event) {
            is AppEvent.OnAuth -> {
                coroutineScope.launch {
                    context.setUser(event.user)
                    Log.i("Auth event", "User ${event.user?.name} authenticated")
                    context.riseEvent(AppEvent.InitUser(event.user))
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

            is AppEvent.NoteSaved -> {
                coroutineScope.launch {
                    if ((event.previousVersion.caption != event.newValue.caption) || (event.previousVersion.type != event.newValue.type)) {
                        val oldType = context.services.noteTypeStorage.types[event.previousVersion.type]
                        val newType = context.services.noteTypeStorage.types[event.newValue.type]!!
                        context.services.tagSearchService.updateTagSuggestions(
                            oldText = if (oldType != null && oldType.tag) event.previousVersion.caption else "",
                            newText = if (newType.tag) event.newValue.caption else "",
                            tagId = event.newValue.id,
                            typeId = newType.id
                        )
                    }
                }
            }
        }
    }
}