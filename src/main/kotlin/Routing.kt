package pro.aibar.sweatsketch

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.dao.DAOFacade
import pro.aibar.sweatsketch.models.LoginRequest
import java.security.MessageDigest
import java.util.*

fun Route.userRoutes(userDAO: DAOFacade) {

    val issuer = environment!!.config.property("jwt.issuer").getString()
    val audience = environment!!.config.property("jwt.audience").getString()
    val secret = environment!!.config.property("jwt.secret").getString()

    get("/") {
        call.respondText("Hello World!")
    }

    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val passwordHash = hashPassword(loginRequest.password)
        if (userDAO.validateUser(loginRequest.username, passwordHash)) {
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("login", loginRequest.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }
    post("/user") {
        val newUser = call.receive<LoginRequest>()
        val passwordHash = hashPassword(newUser.password)
        val user = userDAO.addNewUser(newUser.username, passwordHash)
        if (user != null) {
            call.respondText("User saved with login: ${newUser.username}", status = HttpStatusCode.Created)
        } else {
            call.respond(HttpStatusCode.Conflict, "Username already exists")
        }
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}
