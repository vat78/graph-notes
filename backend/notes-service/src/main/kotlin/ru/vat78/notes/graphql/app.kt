package ru.vat78.notes.graphql

import org.eclipse.microprofile.graphql.*
import ru.vat78.notes.core.api.*
import javax.inject.Inject

@GraphQLApi
class ApplicationEndpoint(@Inject val coreNotes: NoteService) {

    @Mutation("saveNote")
    @Description("Common way to save Note")
    fun saveCoreNote(note: Note) : Note {
        return coreNotes.save(note).log().await().indefinitely()
    }

    @Query("findByCaption")
    @Description("Search notes by caption")
    fun findByCaption(@Name("Caption") caption: String) : List<Note> {
        return coreNotes.findByCaption(caption).collect().asList().log().await().indefinitely()
    }
}