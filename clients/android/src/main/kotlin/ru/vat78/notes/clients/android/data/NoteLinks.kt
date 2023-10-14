package ru.vat78.notes.clients.android.data

import androidx.compose.runtime.Immutable


@Immutable
data class NoteLink(
    val parentId: String,
    val childId: String,
    val deleted: Boolean,
    val lastUpdate: Long = currentTimestamp()
)

interface NoteLinkSyncStorage {
    suspend fun saveLinks(links: Collection<NoteLink>)
    suspend fun getLinksForSync(from: Long, to: Long): List<NoteLink>
}

interface NoteLinkStorage: NoteLinkSyncStorage {
    suspend fun getParentLinksByNoteId(noteId: String): Set<NoteLink>
    suspend fun getChildrenIds(ids: Collection<String>, typeId: Collection<String>): Set<String>

}

data class UpdateNoteLinksEvent(
    val noteId: String,
    val parents: Collection<DictionaryElement>,
    val linkStorage: NoteLinkStorage
): ApplicationEvent {
    override suspend fun handle() {
        val parentIds = parents.map { it.id }.toSet()
        val oldLinks = linkStorage.getParentLinksByNoteId(noteId)
        val timestamp = currentTimestamp()
        val linksForDeletion = oldLinks
            .filter { oldLink ->  !oldLink.deleted && !parentIds.contains(oldLink.parentId) }
            .map { it.copy(deleted = true, lastUpdate = timestamp) }
            .toList()

        val oldParentIds = oldLinks.filter { !it.deleted }.map { it.parentId }.toSet()
        val linksForAdding = parents
            .filter { !oldParentIds.contains(it.id) }
            .map { NoteLink(parentId = it.id, childId = noteId, deleted = false) }
            .toList()

        linkStorage.saveLinks(linksForAdding + linksForDeletion)
    }
}