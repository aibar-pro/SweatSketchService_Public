package pro.aibar.sweatsketch.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.serviceRoutes() {
    get("/health-check") {
        call.respond(HttpStatusCode.OK, "Running")
    }
}