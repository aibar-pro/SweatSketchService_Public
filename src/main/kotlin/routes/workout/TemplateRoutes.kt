package pro.aibar.sweatsketch.routes.workout

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.transactions.transaction
import pro.aibar.sweatsketch.api.workout.NewTemplateRequestDto
import pro.aibar.sweatsketch.api.workout.TemplateVisibility
import pro.aibar.sweatsketch.persistence.dao.workout.TemplateDao
import java.util.UUID

fun Route.templateRoutes(dao: TemplateDao) {

    route("/template") {
        authenticate("jwt-auth") {
            post {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = UUID.fromString(principal.subject)

                val body = call.receive<NewTemplateRequestDto>()
                val id = transaction { dao.createTemplate(body, userId) }
                call.respond(HttpStatusCode.Created, mapOf("templateId" to id))
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val userId = UUID.fromString(principal.subject)

                val visParam = call.request.queryParameters["visibility"]
                val vis = visParam?.let { TemplateVisibility.valueOf(it) }
                val templates = transaction { dao.listTemplates(visibility = vis, userId) }
                call.respond(templates)
            }

            get("{id}") {
                val id = runCatching { UUID.fromString(call.parameters["id"]!!) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val dto = transaction { dao.getTemplate(id) }
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(dto)
            }

            post("{id}/clone") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = UUID.fromString(principal.subject)

                val id = runCatching { UUID.fromString(call.parameters["id"]!!) }.getOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")

                val planId = transaction { dao.cloneTemplate(id, userId) }
                call.respond(HttpStatusCode.Created, mapOf("planId" to planId))
            }
        }
    }
}