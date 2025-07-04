package pro.aibar.sweatsketch

import io.ktor.server.application.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.authentication.configureAuth
import pro.aibar.sweatsketch.database.configureDatabase
import pro.aibar.sweatsketch.persistence.dao.auth.AuthDaoImpl
import pro.aibar.sweatsketch.persistence.dao.profile.UserDaoImpl
import pro.aibar.sweatsketch.persistence.dao.workout.BlueprintDaoImpl
import pro.aibar.sweatsketch.routes.auth.authRoutes
import pro.aibar.sweatsketch.routes.common.serviceRoutes
import pro.aibar.sweatsketch.routes.profile.userRoutes
import pro.aibar.sweatsketch.routes.workout.blueprintRoutes

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.mainModule() {
    configureAuth()
    configureDatabase()
    configureFeatures()

    routing {
        serviceRoutes()
    }
}

fun Application.userModule() {
    val userDao = UserDaoImpl()
    routing {
        userRoutes(userDao)
    }
}

fun Application.authModule() {
    val authDao = AuthDaoImpl()
    routing {
        authRoutes(authDao)
    }
}

fun Application.blueprintModule() {
    val blueprintDao = BlueprintDaoImpl()
    routing {
        blueprintRoutes(blueprintDao)
    }
}
