package pro.aibar.sweatsketch

import io.ktor.server.application.*
import pro.aibar.sweatsketch.dao.DatabaseSingleton

fun Application.configureDatabase(){
    DatabaseSingleton.init()
}