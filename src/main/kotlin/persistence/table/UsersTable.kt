package pro.aibar.sweatsketch.persistence.table

import org.jetbrains.exposed.dao.id.UUIDTable

object UsersTable : UUIDTable("users") {
    val login = varchar("login", 256).uniqueIndex()
    val passwordHash = varchar("passwordHash", 64)
}
