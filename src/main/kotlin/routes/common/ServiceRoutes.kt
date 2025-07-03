package pro.aibar.sweatsketch.routes.common

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.api.common.ResponseMessageDto

fun Route.serviceRoutes() {
    get("/health-check") {
        call.respond(HttpStatusCode.OK, ResponseMessageDto("Running"))
    }
}