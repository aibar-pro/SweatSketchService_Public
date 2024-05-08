package pro.aibar.sweatsketch.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

data class User(val username: String, val passwordHash: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

object Users: Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 256)
    val passwordHash = varchar("passwordHash", 64)

    override val primaryKey = PrimaryKey(id)
}