package ru.vat78.notes.core.storage

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.jboss.logging.Logger
import org.neo4j.driver.Driver
import org.neo4j.driver.Record
import org.neo4j.driver.Values
import org.neo4j.driver.reactive.RxSession
import ru.vat78.notes.core.api.Note
import ru.vat78.notes.core.api.NoteType
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Qualifier

internal interface NoteStorage {
    fun save(note: Note) : Multi<Note>
    fun findByCaption(caption: String) : Multi<Note>
}

@Qualifier
annotation class MainDB

@ApplicationScoped
internal class StubNoteStorage : NoteStorage {
    val storage : MutableList<Note> = mutableListOf()

    override fun save(note: Note): Multi<Note> {
        return Multi.createFrom().item(note).invoke { n ->
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

@MainDB
@ApplicationScoped
internal class Neo4JStorage(@Inject val driver: Driver) : NoteStorage {
    private val log: Logger = Logger.getLogger(javaClass)

    override fun save(note: Note): Multi<Note> {
        val outputResource = Multi.createFrom().resource(driver::rxSession) {
            it.writeTransaction { tr ->
                val result = tr.run(
                    "CREATE (n:Note {id: \$id, type: \$type, caption: \$caption, text: \$text}) RETURN n",
                    Values.parameters("id", note.id, "type", note.type.name, "caption", note.caption, "text", note.text)
                )
                Multi.createFrom().publisher(result.records())
                    .map(this::buildNoteFromRecord)
                    .select().first()
            }
        }
        return outputResource.withFinalizer(this::closeSession)
    }

    override fun findByCaption(caption: String): Multi<Note> {
        return  Multi.createFrom().resource(driver::rxSession) {
            it.readTransaction { tr ->
                val result = tr.run(
                    "MATCH (n:Note) WHERE n.caption CONTAINS \$caption RETURN n",
                    Values.parameters("caption", caption)
                )
                Multi.createFrom().publisher(result.records())
                    .map(this::buildNoteFromRecord)
            }
        }.withFinalizer(this::closeSession)
    }

    private fun buildNoteFromRecord(record: Record): Note {
        log.info("Gotten this record: $record")
        val node = record.get(0).asNode()
        return Note(
            node.get("id").asString(), NoteType.valueOf(node.get("type").asString()),
            node.get("caption").asString(), node.get("text").asString()
        )
    }

    private fun closeSession(session: RxSession) {
        session.close<Note>()
    }
}