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
                    Log.i("Auth event", "User ${event.user?.name} authenticated")
                    context.setUser(event.user)
                }
            }

            is AppEvent.NoteSaved -> {
                coroutineScope.launch {
                    if ((event.previousVersion.caption != event.newValue.caption) || (event.previousVersion.type != event.newValue.type)) {
                        val oldType = event.previousVersion.type
                        val newType = event.newValue.type
                        context.services.tagSearchService.updateTagSuggestions(
                            oldText = if (oldType.tag) event.previousVersion.caption else "",
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