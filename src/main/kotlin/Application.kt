package pro.aibar.sweatsketch

import io.ktor.server.application.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.authentication.configureAuth
import pro.aibar.sweatsketch.dao.AuthDAOFacadeImpl
import pro.aibar.sweatsketch.dao.UserDAOFacadeImpl
import pro.aibar.sweatsketch.database.configureDatabase
import pro.aibar.sweatsketch.routes.userRoutes
import pro.aibar.sweatsketch.routes.authRoutes
import pro.aibar.sweatsketch.routes.serviceRoutes

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.mainModule() {
    configureAuth()
    configureDatabase()
    configureFeatures()

    routing {
        serviceRoutes()
    }
}

fun Application.userModule() {
    val userDao = UserDAOFacadeImpl()
    routing {
        userRoutes(userDao)
    }
}

fun Application.authModule() {
    val authDao = AuthDAOFacadeImpl()
    routing {
        authRoutes(authDao)
    }
}
