package pro.aibar.sweatsketch.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.*


fun Application.configureAuth() {
    val serviceRealm = environment.config.property("jwt.realm").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val refreshTokenSecret = environment.config.property("jwt.refreshSecret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    install(Authentication) {
        jwt("jwt-auth") {
            realm = serviceRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                val isNotExpired = credential.payload.expiresAt?.after(Date()) == true
                val userId = runCatching { UUID.fromString(credential.subject) }.getOrNull()

                if (isNotExpired && userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }

        jwt("refresh-jwt") {
            realm = serviceRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(refreshTokenSecret))
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val isNotExpired = credential.payload.expiresAt?.after(Date()) == true
                val userId = runCatching { UUID.fromString(credential.subject) }.getOrNull()

                if (isNotExpired && userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}