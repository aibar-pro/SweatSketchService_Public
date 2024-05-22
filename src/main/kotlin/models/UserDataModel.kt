package pro.aibar.sweatsketch.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

data class UserDataModel(val login: String, val passwordHash: String)

object Users: IntIdTable() {
    val login = varchar("login", 256).uniqueIndex()
    val passwordHash = varchar("passwordHash", 64)
}

fun ResultRow.toUserDataModel(): UserDataModel {
    return UserDataModel(
        login = this[Users.login],
        passwordHash = this[Users.passwordHash]
    )
}