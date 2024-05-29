package pro.aibar.sweatsketch.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.models.ResponseMessageModel

fun Route.serviceRoutes() {
    get("/health-check") {
        call.respond(HttpStatusCode.OK, ResponseMessageModel("Running"))
    }
}