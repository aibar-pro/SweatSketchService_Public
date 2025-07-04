package pro.aibar.sweatsketch.database

import io.ktor.server.application.*

fun Application.configureDatabase() {
    DatabaseSingleton.init()
}