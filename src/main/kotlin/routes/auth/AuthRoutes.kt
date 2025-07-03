package pro.aibar.sweatsketch.routes.auth

import api.auth.AuthTokenDto
import api.auth.api.auth.Installation
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.api.auth.UserCredentialDto
import pro.aibar.sweatsketch.api.common.ResponseMessageDto
import pro.aibar.sweatsketch.persistence.dao.auth.AuthDao
import java.security.MessageDigest
import java.util.*

fun Route.authRoutes(authDao: AuthDao) {

    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val tokenSecret = environment.config.property("jwt.secret").getString()
    val refreshTokenSecret = environment.config.property("jwt.refreshSecret").getString()

    val maxInstallations = environment.config.property("app.maxInstallations").getString().toInt()

    fun generateToken(userId: UUID, deviceId: String): AuthTokenDto {
        val expiresIn = System.currentTimeMillis() + 3_600_000 // 1 hour
        val accessToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("deviceId", deviceId)
            .withExpiresAt(Date(expiresIn))
            .sign(Algorithm.HMAC256(tokenSecret))
        val refreshToken = JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("deviceId", deviceId)
            .withExpiresAt(Date(System.currentTimeMillis() + 7_776_000_000))  // 90 days
            .sign(Algorithm.HMAC256(refreshTokenSecret))
        return AuthTokenDto(accessToken, refreshToken, expiresIn.toULong())
    }

    /*

    login: credentials + installId (header http)
    request -> login/passwd? -> DB: login -> installIds -> add installId / do nothing -> generate token with installId in payload

    refreshToken: refreshT
    request -> payload -> installId, login -> DB: login -> installIds -> compare? -> reject / respond with generate token

     */

    post("/auth/login") {
        val userCredentialModel = call.receive<UserCredentialDto>()
        val passwordHash = hashPassword(userCredentialModel.password)
        val userAgent = call.request.userAgent() ?: "unknown"
        val host = call.request.local.remoteHost
        val deviceId = call.request.header("deviceId")
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing deviceId")

        val userId = authDao.validateUserAndGetId(
            login = userCredentialModel.login,
            passwordHash = passwordHash
        ) ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")

        val token = generateToken(userId, deviceId)
        val installation = Installation(deviceId, userAgent, host, token.refreshToken)
        authDao.addOrUpdateUserSession(
            userId,
            installation,
            maxInstallations
        )
        call.respond(HttpStatusCode.OK, token)
    }

    authenticate("refresh-jwt") {
        post("/auth/refresh-token") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val userId = UUID.fromString(principal.subject)
            val deviceId = principal.payload.getClaim("deviceId").asString()

            val rawToken = call.request.header("Authorization")?.removePrefix("Bearer ")
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (authDao.validateUserSession(userId, deviceId, rawToken)) {
                val token = generateToken(userId, deviceId)
                authDao.updateRefreshToken(userId, deviceId, token.refreshToken)
                call.respond(HttpStatusCode.OK, token)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }

    authenticate("jwt-auth") {
        delete("/auth/sessions") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val userId = UUID.fromString(principal.subject)

            val deviceId = principal.payload.getClaim("deviceId").toString()
            val headerDeviceId =
                call.request.header("deviceId")
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (deviceId != headerDeviceId) {
                return@delete call.respond(HttpStatusCode.Unauthorized)
            }

            authDao.deleteSession(userId, deviceId)
            call.respond(HttpStatusCode.OK, ResponseMessageDto("Session deleted"))
        }

        delete("/auth/sessions/all") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val userId = UUID.fromString(principal.subject)
            if (userId.toString().isEmpty()) {
                return@delete call.respond(HttpStatusCode.Unauthorized)
            }

            authDao.deleteAllSessions(userId)
            call.respond(HttpStatusCode.OK, ResponseMessageDto("All sessions terminated"))
        }
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}
