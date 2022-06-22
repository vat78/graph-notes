package ru.vat78.notes.graphql

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ru.vat78.notes.core.api.Note
import ru.vat78.notes.core.storage.NoteStorage
import javax.annotation.Priority
import javax.enterprise.inject.Alternative
import javax.inject.Inject
import javax.inject.Singleton


@QuarkusTest
@TestMethodOrder(OrderAnnotation::class)
internal class ApplicationEndpointTest {

    @Inject
    lateinit var storage: NoteStorage

    @Test
    @Order(1)
    fun createNote() {
        val requestBody = "{\"query\": \"mutation{" +
                "  saveNote(note:{" +
                "    caption:\\\"New test\\\"," +
                "    type:COMMON," +
                "    text:\\\"Text of the new note\\\"" +
                "  })" +
                "  {" +
                "    id, caption" +
                "  }" +
                "}\"}"

        given()
            .body(requestBody)
            .contentType(ContentType.JSON)
            .post("/graphql/")
            .then()
            .contentType(ContentType.JSON)
            .body("data.saveNote.caption", `is`("New test"))
            .statusCode(200)
    }

    @Test
    @Order(2)
    fun queryNote() {
        val requestBody = "{\"query\": \"query{ findByCaption(Caption:\\\"test\\\"){id, caption}}\"}"

        given()
            .body(requestBody)
            .contentType(ContentType.JSON)
            .post("/graphql/")
            .then()
            .contentType(ContentType.JSON)
            .body("data.findByCaption[0].caption", `is`("New test"))
            .statusCode(200)
    }
}

@Alternative
@Priority(1)
@Singleton
internal class StubNoteStorage : NoteStorage {
    val storage : MutableList<Note> = mutableListOf()

    override fun save(note: Note): Uni<Note> {
        return Uni.createFrom().item(note).invoke() { n ->
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