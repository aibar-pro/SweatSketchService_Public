package pro.aibar.sweatsketch.persistence.entity

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.persistence.table.UsersTable

data class UserDataModel(val login: String, val passwordHash: String)

fun ResultRow.toUserDataEntity(): UserDataModel {
    return UserDataModel(
        login = this[UsersTable.login],
        passwordHash = this[UsersTable.passwordHash]
    )
}
