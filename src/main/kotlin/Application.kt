package pro.aibar.sweatsketch

import io.ktor.server.application.*
import io.ktor.server.routing.*
import pro.aibar.sweatsketch.dao.DAOFacadeImpl

//fun main() {
//    embeddedServer(Netty, port = 8080) {
//        configureRouting()
//        configureFeatures()
//    }.start(wait = true)
//}

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.userModule() {
    configureAuth()
    configureDatabase()
    configureFeatures()

    val userDao = DAOFacadeImpl()
    routing {
        userRoutes(userDao)
    }
}