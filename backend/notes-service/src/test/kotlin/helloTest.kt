import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class helloTest {
    @Test
    fun testHelloEndpoint() {
        given()
            .`when`().get("/hello/test")
            .then()
            .statusCode(200)
            .body("message", equalTo("hello test"))
    }
}