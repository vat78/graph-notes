package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
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

    @Query("DELETE FROM links")
    fun cleanup(): Int
}