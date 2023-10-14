package ru.vat78.notes.clients.android.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

@Immutable
data class NoteType(
    val name: String = "",
    val icon: String = "Note",
    val tag: Boolean = true,
    val hierarchical: Boolean = false,
    val symbol: Char = '#',
    val defaultStart: TimeDefault = TimeDefault.NOW,
    val defaultFinish: TimeDefault = TimeDefault.NOW,
    val id: String = UUID.randomUUID().toString(),
    val default: Boolean = false,
)

fun getTypeByFirstSymbol(text: String): NoteType? {
    if (text.isBlank()) {
        return null
    }
    val tagSymbol = text.first()
    if (tagSymbol == '#') {
        return null
    }
    return NoteTypes.types.values.firstOrNull { it.symbol == tagSymbol }
}

fun getIcon(type: NoteType?): ImageVector {
    return type?.icon?.let { TmpIcons[it] } ?: Icons.Filled.Note
}

interface NoteTypeStorage {
    suspend fun getTypes(): Collection<NoteType>
    suspend fun save(type: NoteType)
}

object NoteTypes {
    private var _types: Map<String, NoteType> = mapOf()
    val types: Map<String, NoteType>
        get() = _types

    private var _tagSymbols: Set<Char> = setOf()
    val tagSymbols: Set<Char>
        get() = _tagSymbols

    fun getNoteTypeById(id: String): NoteType {
        return _types[id]!!
    }

    internal fun setTypes(types: Collection<NoteType>) {
        _types = types.associateBy { it.id }
        _tagSymbols = _types.values.map { it.symbol }.toSet()
    }
}

data class TypeSyncEvent(
    val internalNoteTypeStorage: NoteTypeStorage,
    val remoteNoteTypeStorage: NoteTypeStorage
): ApplicationEvent {
    override suspend fun handle() {
        // ToDo: now  internal data has priority. Need to change it when types will be modifiable
        var listOfTypes = internalNoteTypeStorage.getTypes()
        if (listOfTypes.isNotEmpty()) {
            NoteTypes.setTypes(listOfTypes)
            GlobalEventHandler.sendEvent(TypeSaveEvent(remoteNoteTypeStorage, listOfTypes))
            return
        }

        listOfTypes = remoteNoteTypeStorage.getTypes()
        if (listOfTypes.isEmpty()) {
            listOfTypes = defaultTypes
            GlobalEventHandler.sendEvent(TypeSaveEvent(remoteNoteTypeStorage, listOfTypes))
        }
        NoteTypes.setTypes(listOfTypes)
        GlobalEventHandler.sendEvent(TypeSaveEvent(internalNoteTypeStorage, listOfTypes))
    }
}

data class TypeSaveEvent(
    val storage: NoteTypeStorage,
    val types: Collection<NoteType>
): ApplicationEvent {
    override suspend fun handle() {
        types.forEach{ type -> storage.save(type) }
    }
}

private val TmpIcons : Map<String, ImageVector> = mapOf(
    Pair("Note", Icons.Filled.Note),
    Pair("Check", Icons.Filled.Check),
    Pair("Tag", Icons.Filled.Tag),
    Pair("People", Icons.Filled.People),
    Pair("House", Icons.Filled.House),
    Pair("Phone", Icons.Filled.Phone),
)

private val defaultTypes = listOf(
    NoteType(
        name = "Notes",
        icon = "Note",
        tag = false,
        hierarchical = false,
        symbol = '-',
        defaultStart = TimeDefault.PREVIOUS_NOTE,
        defaultFinish = TimeDefault.NOW,
        default = true
    ),
    NoteType(
        name = "Tasks",
        icon = "Check",
        tag = true,
        hierarchical = true,
        symbol = '&',
        defaultStart = TimeDefault.NOW,
        defaultFinish = TimeDefault.NEXT_MONTH
    ),
    NoteType(
        name = "Tags",
        icon = "Tag",
        tag = true,
        hierarchical = false,
        symbol = '#',
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
    NoteType(
        name = "Persons",
        icon = "People",
        tag = true,
        hierarchical = false,
        symbol = '@',
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
    NoteType(
        name = "Organisations",
        icon = "House",
        tag = true,
        hierarchical = false,
        symbol = '^',
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
    NoteType(
        name = "Phones",
        icon = "Phone",
        tag = true,
        hierarchical = false,
        symbol = '+',
        defaultStart = TimeDefault.START_OF_TIME,
        defaultFinish = TimeDefault.END_OF_TIME
    ),
)