package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import ru.vat78.notes.clients.android.data.room.entity.LinkEntity

@Dao
interface LinksDao {
    @Upsert
    fun save(vararg links: LinkEntity)

    @Query("UPDATE links SET deleted = 1, lastUpdate = CURRENT_TIMESTAMP WHERE child = :child")
    fun deleteLinksByChild(child: String)

    @Query("SELECT * FROM links WHERE child = :child and deleted <> 1")
    fun getParents(child: String): List<LinkEntity>

    @Query("SELECT l.* FROM links l JOIN notes n ON l.child = n.id AND n.typeId in (:types) WHERE l.parent in (:parents) and l.deleted <> 1")
    fun getChildren(parents: Collection<String>, types: Collection<String>): List<LinkEntity>

    @Query("DELETE FROM links")
    fun cleanup(): Int

    @Query("SELECT * FROM links WHERE lastUpdate BETWEEN :from AND :to")
    fun getLinksForSync(from: Long, to: Long): List<LinkEntity>

    @Delete
    fun delete(vararg links: LinkEntity)
}