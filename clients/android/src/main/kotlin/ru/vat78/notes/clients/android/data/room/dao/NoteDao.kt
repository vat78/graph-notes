package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import ru.vat78.notes.clients.android.data.room.entity.NoteEntity

@Dao
interface NoteDao {

    @Upsert
    fun save(note: NoteEntity)

    @Upsert
    fun saveAll(vararg notes: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    fun findById(id: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE caption = :caption")
    fun findByCaption(caption: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (select child from links where parent = :id and not deleted)")
    fun findChildren(id: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (select parent from links where child = :id and not deleted)")
    fun findParents(id: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE typeId in (:types) and root")
    fun findOnlyRoots(types: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (:ids) and typeId in (:types) and root")
    fun findByIdsOnlyRoots(types: Collection<String>, ids: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE typeId in (:types)")
    fun findByTypes(types: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (:ids) and typeId in (:types)")
    fun findByIds(types: Collection<String>, ids: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (SELECT tagId FROM suggestions WHERE wordId in (:wordIds) and tagId not in (:excludedTags) and typeId not in (:excludedTypes))")
    fun findTagsForSuggestions(wordIds: List<Long>, excludedTypes: List<String>, excludedTags: Collection<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id in (SELECT tagId FROM suggestions WHERE wordId in (:wordIds) and tagId not in (:excludedTags) and typeId = :type)")
    fun findTagsForSuggestions(wordIds: List<Long>, type: String, excludedTags: Collection<String>): List<NoteEntity>

    @Query("DELETE FROM notes")
    fun cleanup(): Int

    @Query("UPDATE notes SET suggestions = :suggestions WHERE id = :id")
    fun updateSuggestions(id: String, suggestions: String)

    @Query("SELECT * FROM notes WHERE lastUpdate BETWEEN :from AND :to")
    fun getNotesForSync(from: Long, to: Long): List<NoteEntity>

    @Delete
    fun delete(vararg notes: NoteEntity)
}