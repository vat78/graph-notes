
package ru.vat78.notes.clients.android

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.plus
import ru.vat78.notes.clients.android.data.ExternalStorage
import ru.vat78.notes.clients.android.data.GlobalEventHandler
import ru.vat78.notes.clients.android.data.TypeSyncEvent
import ru.vat78.notes.clients.android.data.User
import ru.vat78.notes.clients.android.data.room.RoomContext
import ru.vat78.notes.clients.android.data.syncData
import ru.vat78.notes.clients.android.firebase.firestore.FirestoreContext
import java.time.Instant
import java.util.concurrent.TimeUnit

private var externalStorage: ExternalStorage? = null
private val _services : RoomContext = RoomContext()

class ApplicationContext {

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    private val workManager = WorkManager.getInstance()

    val services: RoomContext
        get() = _services

    val currentUser = MutableSharedFlow<User?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val eventHandler = AppEventHandler(this, MainScope().plus(CoroutineName("AppEventHandler")))

    fun riseEvent(event: AppEvent) {
        eventHandler.riseEvent(event)
    }

    suspend fun setUser(user: User?) {
        externalStorage = if (user == null) null else FirestoreContext(user)
        val newUser = user ?: services.user
        services.userStorage.saveUser(newUser)
        initSync()
        currentUser.emit(newUser)
        Log.i("Application context", "The current user has been changed on ${user?.name}")
    }

    private suspend fun initSync() {
        if (externalStorage == null) {
            workManager.cancelUniqueWork("SYNC")
            return
        }
        GlobalEventHandler.sendEvent(TypeSyncEvent(services.NoteTypeRepository(), externalStorage!!.noteTypeStorage))
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork("SYNC", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, syncRequest)
    }
}

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val timestamp = Instant.now().epochSecond
        if (externalStorage != null) {
            Log.i("SyncWorker", "Data sync started at $timestamp")
            syncData(_services, externalStorage!!, timestamp)
        }
        return Result.success()
    }
}