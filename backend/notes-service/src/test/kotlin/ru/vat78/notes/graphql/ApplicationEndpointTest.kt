package ru.vat78.notes.graphql

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ru.vat78.notes.core.storage.NoteStorage
import javax.inject.Inject


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
            .statusCode(200)
    }
}