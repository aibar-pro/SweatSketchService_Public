package pro.aibar.sweatsketch.routes

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
import pro.aibar.sweatsketch.models.RefreshTokenModel
import pro.aibar.sweatsketch.models.UserCredentialModel
import java.security.MessageDigest
import java.util.*

fun Route.authRoutes(authDAO: AuthDAOFacade) {

    val issuer = environment!!.config.property("jwt.issuer").getString()
    val audience = environment!!.config.property("jwt.audience").getString()
    val tokenSecret = environment!!.config.property("jwt.secret").getString()
    val refreshTokenSecret = environment!!.config.property("jwt.refreshSecret").getString()

    fun generateToken(login: String): AuthTokenModel {
        val expiresIn = System.currentTimeMillis() + 86_400_000 // 24 hours
        val accessToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("login", login)
            .withExpiresAt(Date(expiresIn))
            .sign(Algorithm.HMAC256(tokenSecret))
        val refreshToken = JWT.create()
            .withIssuer(issuer)
            .withClaim("login", login)
            .withExpiresAt(Date(System.currentTimeMillis() + 7_776_000_000))  // 90 days
            .sign(Algorithm.HMAC256(refreshTokenSecret))
        return AuthTokenModel(accessToken, refreshToken, expiresIn.toULong())
    }

    post("/auth/login") {
        val userCredentialModel = call.receive<UserCredentialModel>()
        val passwordHash = hashPassword(userCredentialModel.password)
        if (authDAO.validateUser(userCredentialModel.login, passwordHash)) {
            val token = generateToken(userCredentialModel.login)
            authDAO.addRefreshToken(userCredentialModel.login, RefreshTokenModel(token.refreshToken))

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

            if (authDAO.validateRefreshToken(login, refreshTokenModel)) {
                val token = generateToken(login)
                authDAO.updateRefreshToken(login, RefreshTokenModel(token.refreshToken), refreshTokenModel)
                call.respond(HttpStatusCode.OK, token)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
        }
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}
