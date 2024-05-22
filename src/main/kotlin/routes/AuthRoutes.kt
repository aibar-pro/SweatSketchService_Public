package pro.aibar.sweatsketch.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.dao.AuthDAOFacade
import pro.aibar.sweatsketch.models.AuthTokenModel
import pro.aibar.sweatsketch.models.RefreshTokenModel
import pro.aibar.sweatsketch.models.UserCredentialModel
import java.security.MessageDigest
import java.util.*

fun Route.authRoutes(authDAO: AuthDAOFacade) {

    val issuer = environment!!.config.property("jwt.issuer").getString()
    val audience = environment!!.config.property("jwt.audience").getString()
    val tokenSecret = environment!!.config.property("jwt.secret").getString()
    val refreshTokenSecret = environment!!.config.property("jwt.refreshSecret").getString()

    get("/health-check") {
        call.respond(HttpStatusCode.OK, "Running")
    }

    post("/auth/login") {
        val userCredentialModel = call.receive<UserCredentialModel>()
        val passwordHash = hashPassword(userCredentialModel.password)
        if (authDAO.validateUser(userCredentialModel.login, passwordHash)) {
            val expiresIn = System.currentTimeMillis() + 600_000
            val accessToken = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("login", userCredentialModel.login)
                .withExpiresAt(Date(expiresIn))
                .sign(Algorithm.HMAC256(tokenSecret))
            val refreshToken = JWT.create()
                .withIssuer(issuer)
                .withClaim("login", userCredentialModel.login)
                .withExpiresAt(Date(System.currentTimeMillis() + 2_592_000_000))  // 30 days
                .sign(Algorithm.HMAC256(refreshTokenSecret))

            val token = AuthTokenModel(accessToken, refreshToken, expiresIn.toULong())
            authDAO.addRefreshToken(RefreshTokenModel(userCredentialModel.login, refreshToken))

            call.respond(HttpStatusCode.OK, token)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }

    post("/auth/refresh-token") {
        val refreshTokenModel = call.receive<RefreshTokenModel>()
        if (authDAO.validateRefreshToken(refreshTokenModel)) {
            val expiresIn = System.currentTimeMillis() + 600_000
            val accessToken = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("login", refreshTokenModel.login)
                .withExpiresAt(Date(expiresIn))
                .sign(Algorithm.HMAC256(tokenSecret))
            val refreshToken = JWT.create()
                .withIssuer(issuer)
                .withClaim("login", refreshTokenModel.login)
                .withExpiresAt(Date(System.currentTimeMillis() + 2_592_000_000))  // 30 days
                .sign(Algorithm.HMAC256(refreshTokenSecret))

            val token = AuthTokenModel(accessToken, refreshToken, expiresIn.toULong())
            call.respond(HttpStatusCode.OK, token)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}
