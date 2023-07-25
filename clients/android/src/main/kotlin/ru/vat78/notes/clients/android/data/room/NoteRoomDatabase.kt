package ru.vat78.notes.clients.android.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.vat78.notes.clients.android.data.room.dao.LinksDao
import ru.vat78.notes.clients.android.data.room.dao.NoteDao
import ru.vat78.notes.clients.android.data.room.dao.NoteTypeDao
import ru.vat78.notes.clients.android.data.room.dao.SuggestionDao
import ru.vat78.notes.clients.android.data.room.dao.UserDao
import ru.vat78.notes.clients.android.data.room.dao.WordDao
import ru.vat78.notes.clients.android.data.room.entity.NoteEntity
import ru.vat78.notes.clients.android.data.room.entity.NoteTypeEntity
import ru.vat78.notes.clients.android.data.room.entity.LinkEntity
import ru.vat78.notes.clients.android.data.room.entity.SuggestionEntity
import ru.vat78.notes.clients.android.data.room.entity.UserEntity
import ru.vat78.notes.clients.android.data.room.entity.WordEntity

@Database(
    entities = [
        NoteTypeEntity::class,
        NoteEntity::class,
        UserEntity::class,
        LinkEntity::class,
        WordEntity::class,
        SuggestionEntity::class],
    version = 3)
abstract class NoteRoomDatabase: RoomDatabase() {
    abstract fun noteTypeDao(): NoteTypeDao
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun linkDao(): LinksDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun wordDao(): WordDao
}