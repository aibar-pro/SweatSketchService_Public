package pro.aibar.sweatsketch.routes.profile

import UserDao
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.api.auth.UserCredentialDto
import pro.aibar.sweatsketch.api.common.ResponseMessageDto
import pro.aibar.sweatsketch.api.profile.UserProfileDto
import pro.aibar.sweatsketch.routes.auth.hashPassword
import java.util.UUID

fun Route.userRoutes(userDao: UserDao) {
    route("/user") {
        post {
            val newUser = call.receive<UserCredentialDto>()
            val passwordHash = hashPassword(newUser.password)
            val user = userDao.addNewUser(newUser.login, passwordHash)
            if (user != null) {
                call.respond(HttpStatusCode.Created, ResponseMessageDto("User saved with login: ${newUser.login}"))
            } else {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
            }
        }

        authenticate("jwt-auth") {
            route("/profile") {
                post {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val userId = UUID.fromString(principal.subject)

                    val userProfile = call.receive<UserProfileDto>()
                    userDao.addUserProfile(userId, userProfile)
                    call.respond(
                        HttpStatusCode.Created,
                        ResponseMessageDto("User profile created")
                    )
                }

                get {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val userId = UUID.fromString(principal.subject)

                    val userProfile = userDao.getUserProfile(userId)
                    if (userProfile != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            userProfile
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            "User profile not found"
                        )
                    }
                }

                put {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized)
                    val userId = UUID.fromString(principal.subject)

                    val userProfileUpdate = call.receive<UserProfileDto>()
                    val updatedProfile = userDao.updateUserProfile(userId, userProfileUpdate)
                    if (updatedProfile != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            ResponseMessageDto("User profile updated")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            "User profile not found"
                        )
                    }
                }
            }
        }
    }
}