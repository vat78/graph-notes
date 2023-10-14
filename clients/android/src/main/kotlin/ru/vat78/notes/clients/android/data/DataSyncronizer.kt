package ru.vat78.notes.clients.android.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.room.RoomContext
import ru.vat78.notes.clients.android.data.room.entity.NoteEntity

suspend fun syncData(localStorage: RoomContext, externalStorage: ExternalStorage, timestamp: Long) {
    withContext(Dispatchers.IO) {
        val deviceId = localStorage.userStorage.getCurrentUser()!!.deviceId
        val lastSyncTime = externalStorage.userStorage.getLastSyncTimestamp(externalStorage.user.id, deviceId) ?: 0
        val stats = mutableMapOf<String, Int>()
        Log.i("DataSynchronizer", "Start syncing for device $deviceId from $lastSyncTime to $timestamp")

        val extNotes = externalStorage.noteStorage.getNotesForSync(lastSyncTime, timestamp)
        val deletedNotes = extNotes.filter { it.deleted }
        stats["deletedExtNotes"] = deletedNotes.size
        val changedNotes = extNotes.filter { !it.deleted }
        stats["changedExtNotes"] = changedNotes.size
        Log.i("DataSynchronizer", "Got ${extNotes.size} notes from external, ${changedNotes.size} for updating and ${deletedNotes.size} for deleting")
        localStorage.noteStorage.saveNotes(*changedNotes.map { NoteEntity(it, cleanLastUpdate = true) }.toTypedArray())
        localStorage.noteStorage.deleteNotes(*deletedNotes.map { NoteEntity(it, cleanLastUpdate = true) }.toTypedArray())
        extNotes.forEach { note -> GlobalEventHandler.sendEvent(UpdateTagWordsEvent(note, localStorage.suggestionStorage)) }


        val extLinks = externalStorage.linkStorage.getLinksForSync(lastSyncTime, timestamp)
        val deletedLinks = extLinks.filter { it.deleted }.toList()
        stats["deletedExtLinks"] = deletedLinks.size
        val changedLinks = extLinks.filter { !it.deleted }.toList()
        stats["changedExtLinks"] = changedLinks.size
        Log.i("DataSynchronizer", "Got ${extLinks.size} links from external, ${changedLinks.size} for updating and ${deletedLinks.size} for deleting")
        localStorage.linkStorage.deleteLinks(deletedLinks)
        localStorage.linkStorage.saveLinks(changedLinks)


        val localNotes = localStorage.NoteRepository().getNotesForSync(lastSyncTime, timestamp)
        Log.i("DataSynchronizer", "Got ${localNotes.size} local notes for syncing")
        stats["changedLocalNotes"] = localNotes.size
        localNotes.forEach { externalStorage.noteStorage.save(it) }
        val localNotesForDelete = localNotes.filter { it.deleted }.map { NoteEntity(it) }.toTypedArray()

        val localLinks = localStorage.linkStorage.getLinksForSync(lastSyncTime, timestamp)
        val deletedLocalLinks = localLinks.filter { it.deleted }.toList()
        val changedLocalLinks = localLinks.filter { !it.deleted }.toList()
        Log.i("DataSynchronizer", "Got ${localLinks.size} links from local, ${changedLocalLinks.size} for updating and ${deletedLocalLinks.size} for deleting")
        stats["changedLocalLinks"] = localLinks.size
        externalStorage.linkStorage.saveLinks(localLinks)
        localStorage.linkStorage.deleteLinks(deletedLocalLinks)
        localStorage.noteStorage.deleteNotes(*localNotesForDelete)

        externalStorage.userStorage.saveLastSyncTimestamp(externalStorage.user.id, deviceId, timestamp, stats)
    }
}