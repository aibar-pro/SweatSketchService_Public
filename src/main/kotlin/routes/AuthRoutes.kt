package pro.aibar.sweatsketch.routes

import Installation
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.dao.AuthDAOFacade
import pro.aibar.sweatsketch.models.AuthTokenModel
import pro.aibar.sweatsketch.models.DeleteSessionRequest
import pro.aibar.sweatsketch.models.RefreshTokenModel
import pro.aibar.sweatsketch.models.UserCredentialModel
import java.security.MessageDigest
import java.util.*

fun Route.authRoutes(authDAO: AuthDAOFacade) {

    val issuer = environment!!.config.property("jwt.issuer").getString()
    val audience = environment!!.config.property("jwt.audience").getString()
    val tokenSecret = environment!!.config.property("jwt.secret").getString()
    val refreshTokenSecret = environment!!.config.property("jwt.refreshSecret").getString()

    val maxInstallations = environment!!.config.property("app.maxInstallations").getString().toInt()

    fun generateToken(login: String, deviceId: String): AuthTokenModel {
        val expiresIn = System.currentTimeMillis() + 3_600_000 // 1 hour
        val accessToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("login", login)
            .withClaim("deviceId", deviceId)
            .withExpiresAt(Date(expiresIn))
            .sign(Algorithm.HMAC256(tokenSecret))
        val refreshToken = JWT.create()
            .withIssuer(issuer)
            .withClaim("login", login)
            .withClaim("deviceId", deviceId)
            .withExpiresAt(Date(System.currentTimeMillis() + 7_776_000_000))  // 90 days
            .sign(Algorithm.HMAC256(refreshTokenSecret))
        return AuthTokenModel(accessToken, refreshToken, expiresIn.toULong())
    }

/*

login: credentials + installId (header http)
request -> login/passwd? -> DB: login -> installIds -> add installId / do nothing -> generate token with installId in payload

refreshToken: refreshT
request -> payload -> installId, login -> DB: login -> installIds -> compare? -> reject / respond with generate token

 */

    post("/auth/login") {
        val userCredentialModel = call.receive<UserCredentialModel>()
        val passwordHash = hashPassword(userCredentialModel.password)
        val userAgent = call.request.userAgent() ?: "unknown"
        val host = call.request.local.remoteHost
        val deviceId = call.request.header("deviceId") ?:
            return@post call.respond(HttpStatusCode.BadRequest, "Missing deviceId")

        if (authDAO.validateUser(userCredentialModel.login, passwordHash)) {
            val token = generateToken(userCredentialModel.login, deviceId)
            val installation = Installation(deviceId, userAgent, host, token.refreshToken)
            authDAO.addOrUpdateUserSession(
                userCredentialModel.login,
                installation,
                maxInstallations
            )
            call.respond(HttpStatusCode.OK, token)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }

    post("/auth/refresh-token") {
        val refreshTokenModel = call.receive<RefreshTokenModel>()

        try {
            val verifier = JWT
                .require(Algorithm.HMAC256(refreshTokenSecret))
                .withIssuer(issuer)
                .build()
            val decodedJWT: DecodedJWT = verifier.verify(refreshTokenModel.refreshToken)
            val login = decodedJWT.getClaim("login").asString()
            val deviceId = decodedJWT.getClaim("deviceId").asString()

            if (authDAO.validateUserSession(login, deviceId, refreshTokenModel.refreshToken)) {
                val token = generateToken(login, deviceId)
                authDAO.updateRefreshToken(login, deviceId, token.refreshToken)
                call.respond(HttpStatusCode.OK, token)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
        }
    }

    delete("/auth/sessions") {
        val deleteRequest = call.receive<DeleteSessionRequest>()
        val deviceId = call.request.header("deviceId") ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing deviceId")
        authDAO.deleteSessionByDeviceId(deleteRequest.login, deviceId)
        call.respond(HttpStatusCode.OK, "Session deleted")
    }

    delete("/auth/sessions/all") {
        val deleteRequest = call.receive<DeleteSessionRequest>()
        authDAO.terminateAllSessionsForUser(deleteRequest.login)
        call.respond(HttpStatusCode.OK, "All sessions terminated")
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}
