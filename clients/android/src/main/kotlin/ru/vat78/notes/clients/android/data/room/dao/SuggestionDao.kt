package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.vat78.notes.clients.android.data.room.entity.SuggestionEntity

@Dao
interface SuggestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg suggestions: SuggestionEntity)

    @Delete
    fun delete(vararg suggestions: SuggestionEntity)

    @Query("DELETE FROM suggestions")
    fun cleanup(): Int

    @Query("DELETE FROM suggestions WHERE tagId = :tagId")
    fun deleteForTag(tagId: String): Int
}