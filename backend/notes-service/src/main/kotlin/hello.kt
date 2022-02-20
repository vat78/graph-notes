import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

data class Greeting(val message: String)

@ApplicationScoped
class GreetingService {
    fun greeting(name: String): Greeting {
        return Greeting("hello $name")
    }
}


@Path("/")
class ReactiveGreetingResource (
    private val service: GreetingService
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hello/{name}")
    fun greeting(@PathParam("name") name: String): Greeting {
        return service.greeting(name)
    }

}