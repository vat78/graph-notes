package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.vat78.notes.clients.android.data.room.entity.NoteTypeEntity

@Dao
interface NoteTypeDao {
    @Upsert
    fun save(type: NoteTypeEntity)

    @Query("SELECT * FROM note_types WHERE id = :id")
    fun findById(id: String): List<NoteTypeEntity>

    @Query("DELETE FROM note_types WHERE id = :id")
    fun delete(id: String)

    @Query("SELECT * FROM note_types")
    fun getAll(): List<NoteTypeEntity>

    @Query("DELETE FROM note_types")
    fun cleanup(): Int
}