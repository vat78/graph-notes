
package ru.vat78.notes.clients.android

import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.data.AppContext
import ru.vat78.notes.clients.android.data.StubAppContext
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.firebase.firestore.FirestoreContext

class ApplicationContext {
    val currentUser = MutableSharedFlow<User?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val stubContext = StubAppContext()

    private var _services : AppContext = stubContext
    val services : AppContext
        get() = _services


    private val eventHandler = AppEventHandler(this, MainScope())

    fun riseEvent(event: AppEvent) {
        eventHandler.riseEvent(event)
    }

    suspend fun setUser(user: User?) {
        _services = if (user == null) stubContext else FirestoreContext(user)
        currentUser.emit(user)
        Log.i("Application context", "The current user has been changed on ${user?.name}")
    }

}