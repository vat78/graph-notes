import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.html.ReactHTML.h1
import react.dom.render

fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")

    render(Fragment.create {
        h1 {
            +"Hello, from Graph Notes!"
        }
    }, container)
}