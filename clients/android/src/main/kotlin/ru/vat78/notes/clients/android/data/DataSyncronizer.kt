package ru.vat78.notes.clients.android.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vat78.notes.clients.android.data.room.RoomContext
import ru.vat78.notes.clients.android.data.room.entity.NoteEntity

suspend fun syncData(localStorage: RoomContext, externalStorage: AppStorage, timestamp: Long) {
    withContext(Dispatchers.IO) {
        val deviceId = localStorage.userStorage.getCurrentUser()!!.deviceId
        val lastSyncTime = externalStorage.userStorage.getLastSyncTimestamp(externalStorage.user.id, deviceId) ?: 0
        Log.i("DataSynchronizer", "Start syncing for device $deviceId from $lastSyncTime to $timestamp")

        val extNotes = externalStorage.noteStorage.getNotesForSync(lastSyncTime, timestamp)
        val deletedNotes = extNotes.filter { it.deleted }
        val changedNotes = extNotes.filter { !it.deleted }
        Log.i("DataSynchronizer", "Got ${extNotes.size} notes from external, ${changedNotes.size} for updating and ${deletedNotes.size} for deleting")
        localStorage.noteStorage.saveNotes(*changedNotes.map { NoteEntity(it, cleanLastUpdate = true) }.toTypedArray())
        localStorage.noteStorage.deleteNotes(*deletedNotes.map { NoteEntity(it, cleanLastUpdate = true) }.toTypedArray())

        val extLinks = externalStorage.noteStorage.getLinksForSync(lastSyncTime, timestamp)
        val deletedLinks = extLinks.filter { it.deleted }.toList()
        val changedLinks = extLinks.filter { !it.deleted }.toList()
        Log.i("DataSynchronizer", "Got ${extLinks.size} links from external, ${changedLinks.size} for updating and ${deletedLinks.size} for deleting")
        localStorage.noteStorage.deleteLinks(deletedLinks)
        localStorage.noteStorage.saveSyncingLinks(changedLinks)
        changedNotes.filter { it.type.tag }.forEach { localStorage.tagSearchService.updateTagSuggestions(it) }


        val localNotes = localStorage.NoteRepository().getNotesForSync(lastSyncTime, timestamp)
        Log.i("DataSynchronizer", "Got ${localNotes.size} local notes for syncing")
        localNotes.forEach { externalStorage.noteStorage.updateNote(it) }
        val localNotesForDelete = localNotes.filter { it.deleted }.map { NoteEntity(it) }.toTypedArray()

        val localLinks = localStorage.NoteRepository().getLinksForSync(lastSyncTime, timestamp)
        val deletedLocalLinks = localLinks.filter { it.deleted }.toList()
        val changedLocalLinks = localLinks.filter { !it.deleted }.toList()
        Log.i("DataSynchronizer", "Got ${localLinks.size} links from local, ${changedLocalLinks.size} for updating and ${deletedLocalLinks.size} for deleting")
        externalStorage.noteStorage.saveSyncingLinks(changedLocalLinks)
        externalStorage.noteStorage.deleteLinks(deletedLocalLinks)
        localStorage.noteStorage.deleteLinks(deletedLocalLinks)
        localStorage.noteStorage.deleteNotes(*localNotesForDelete)

        externalStorage.userStorage.saveLastSyncTimestamp(externalStorage.user.id, deviceId, timestamp)
    }
}