
package ru.vat78.notes.clients.android

import android.util.Log
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.plus
import ru.vat78.notes.clients.android.data.AppContext
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.defaultTypes
import ru.vat78.notes.clients.android.data.room.RoomContext
import ru.vat78.notes.clients.android.firebase.firestore.FirestoreContext

class ApplicationContext {
    val currentUser = MutableSharedFlow<User?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var _externalStorage: AppContext = StubAppContext()
    val externalStorage: AppContext
        get() = _externalStorage

    val services : RoomContext = RoomContext()

    private val eventHandler = AppEventHandler(this, MainScope().plus(CoroutineName("AppEventHandler")))

    fun riseEvent(event: AppEvent) {
        eventHandler.riseEvent(event)
    }

    suspend fun setUser(user: User?) {
        _externalStorage = if (user == null) StubAppContext() else FirestoreContext(user)
        val newUser = user ?: services.user
        services.userStorage.saveUser(newUser)
        reloadTypes()
        currentUser.emit(newUser)
        Log.i("Application context", "The current user has been changed on ${user?.name}")
    }

    private suspend fun reloadTypes() {
        _externalStorage.noteTypeStorage.reload()
        val types = _externalStorage.noteTypeStorage.types.values
        Log.i("Application context", "Types from external $types")
        if (types.isEmpty()) {
            services.syncTypes(defaultTypes)
        } else {
            services.syncTypes(types)
        }
    }

}