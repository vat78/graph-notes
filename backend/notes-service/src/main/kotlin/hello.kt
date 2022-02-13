import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@ApplicationScoped
class GreetingService {
    fun greeting(name: String): String {
        return "hello $name"
    }
}


@Path("/")
class ReactiveGreetingResource {

    @Inject
    @field: Default
    lateinit var service: GreetingService

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello/{name}")
    fun greeting(@PathParam("name") name: String): String {
        return service.greeting(name)
    }

}