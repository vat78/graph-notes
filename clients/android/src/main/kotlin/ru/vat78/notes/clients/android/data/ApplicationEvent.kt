package ru.vat78.notes.clients.android.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

interface ApplicationEvent {
    suspend fun handle()
}

object GlobalEventHandler {

    private val eventQueue = Channel<ApplicationEvent>()

    suspend fun sendEvent(event: ApplicationEvent) {
        eventQueue.send(event)
    }

    fun init() {
        GlobalScope.launch {
            while (true) {
                val event = eventQueue.receive()
                launch {
                    event.handle()
                }
            }
        }
    }
}

data class ValidationResult<T> (
    val data: T? = null,
    val errorCode: Int? = null
)