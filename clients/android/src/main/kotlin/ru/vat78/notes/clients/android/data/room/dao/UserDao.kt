package ru.vat78.notes.clients.android.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.vat78.notes.clients.android.data.room.entity.UserEntity

@Dao
interface UserDao {
    @Upsert
    fun save(type: UserEntity)
    @Query("DELETE FROM users")
    fun cleanup(): Int

    @Query("SELECT * FROM users")
    fun getAll(): List<UserEntity>
}