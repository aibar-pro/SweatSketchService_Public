package pro.aibar.sweatsketch.routes
import UserDAOFacade
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.models.ResponseMessageModel
import pro.aibar.sweatsketch.models.UserCredentialModel
import pro.aibar.sweatsketch.models.UserProfileModel

fun Route.userRoutes(userDAO: UserDAOFacade) {
    post("/user") {
        val newUser = call.receive<UserCredentialModel>()
        val passwordHash = hashPassword(newUser.password)
        val user = userDAO.addNewUser(newUser.login, passwordHash)
        if (user != null) {
            call.respond(HttpStatusCode.Created, ResponseMessageModel("User saved with login: ${newUser.login}"))
        } else {
            call.respond(HttpStatusCode.Conflict, "Username already exists")
        }
    }

    authenticate("jwt-auth") {
        post("/user/profile") {
            val userProfile = call.receive<UserProfileModel>()
            userDAO.addUserProfile(userProfile)
            call.respond(HttpStatusCode.Created, ResponseMessageModel("User profile saved for login: ${userProfile.login}"))
        }

        get("/user/profile/{login}") {
            val login = call.parameters["login"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or malformed login")
            val userProfile = userDAO.getUserProfile(login)
            if (userProfile != null) {
                call.respond(HttpStatusCode.OK, userProfile)
            } else {
                call.respond(HttpStatusCode.NotFound, "User profile not found")
            }
        }

        put("/user/profile/{login}") {
            val login = call.parameters["login"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing or malformed login")
            val userProfileUpdate = call.receive<UserProfileModel>()
            val updatedProfile = userDAO.updateUserProfile(login, userProfileUpdate)
            if (updatedProfile != null) {
                call.respond(HttpStatusCode.OK, ResponseMessageModel("User profile updated for login: $login"))
            } else {
                call.respond(HttpStatusCode.NotFound, "User profile not found")
            }
        }
    }
}