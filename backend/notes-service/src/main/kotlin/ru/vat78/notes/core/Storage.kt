package ru.vat78.notes.core.storage

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import ru.vat78.notes.core.api.Note
import javax.enterprise.context.ApplicationScoped

internal interface NoteStorage {
    fun save(note: Note) : Uni<Note>
    fun findByCaption(caption: String) : Multi<Note>
}

@ApplicationScoped
internal class StubNoteStorage : NoteStorage {
    val storage : MutableList<Note> = mutableListOf()

    override fun save(note: Note): Uni<Note> {
        return Uni.createFrom().item(note).invoke { n ->
            storage.add(n)
            n
        }
    }

    override fun findByCaption(caption: String): Multi<Note> {
        return Uni.createFrom().item(caption)
            .onItem()
            .transformToMulti { c -> Multi.createFrom().iterable(
                storage.filter { it.caption.contains(c, ignoreCase = true) }) }
    }
}