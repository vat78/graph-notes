package ru.vat78.notes.graphql

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ApplicationEndpointTest {

    @Test
    fun createNote() {
        val requestBody = "{\"query\":" +
                "\"" +
                "mutation saveNote { " +
                "saveCoreNote" +
                "(note: " +
                "{" +
                "type: \\\"Common\\\" " +
                "caption: \\\"New test\\\" " +
                "text:  \\\"Text of the new note\\\" " +
                "}" +
                ")" +
                "{" +
                "id " +
                "caption "
                "}" +
                "}" +
                "\"" +
                "}"

        given()
            .body(requestBody)
            .contentType(ContentType.JSON)
            .post("/graphql/")
            .then()
            .contentType(ContentType.JSON)
            .body("data.saveCoreNote.caption", `is`("New test"))
            .statusCode(200)
    }
}