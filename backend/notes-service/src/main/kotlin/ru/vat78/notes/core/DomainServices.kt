package ru.vat78.notes.core

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
internal class NoteServiceImpl(@Inject val storage: NoteStorage) : NoteService {

    override fun save(note: Note): Uni<Note> {
        return storage.save(note)
    }

    override fun findByCaption(caption: String): Multi<Note> {
        return storage.findByCaption(caption)
    }
}