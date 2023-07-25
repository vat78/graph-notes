package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.vat78.notes.clients.android.data.room.entity.NoteEntity

@Dao
interface NoteDao {

    @Upsert
    fun save(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    fun findById(id: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (select child from links where parent = :id and not deleted)")
    fun findChildren(id: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (select parent from links where child = :id and not deleted)")
    fun findParents(id: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE typeId in (:types) and root")
    fun findOnlyRoots(types: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (:ids) and typeId in (:types) and root")
    fun findByIdsOnlyRoots(types: Collection<String>, ids: List<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE typeId in (:types)")
    fun findByTypes(types: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (:ids) and typeId in (:types)")
    fun findByIds(types: Collection<String>, ids: List<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (SELECT tagId FROM suggestions WHERE wordId in (:wordIds) and tagId not in (:excludedTags) and typeId not in (:excludedTypes))")
    fun findTagsForSuggestions(wordIds: List<Long>, excludedTypes: List<String>, excludedTags: Collection<String>): List<NoteEntity>

    @Query("DELETE FROM notes")
    fun cleanup(): Int
}