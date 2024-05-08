package pro.aibar.sweatsketch.models

import org.jetbrains.exposed.dao.id.IntIdTable

data class User(val username: String, val passwordHash: String)

object Users: IntIdTable() {
    val username = varchar("username", 256).uniqueIndex()
    val passwordHash = varchar("passwordHash", 64)
}