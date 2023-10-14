package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import ru.vat78.notes.clients.android.data.room.entity.WordEntity

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg words: WordEntity)

    @Delete
    fun delete(vararg words: WordEntity)

    @Query("SELECT * FROM words WHERE word IN (:search)")
    fun findWords(search: Collection<String>) : List<WordEntity>

    @Query("DELETE FROM words")
    fun cleanup(): Int
}