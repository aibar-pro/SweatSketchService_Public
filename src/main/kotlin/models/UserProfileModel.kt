package pro.aibar.sweatsketch.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class UserProfileModel (
    val login: String,
    val username: String?,
    val age: Int?,
    val height: Double?,
    val weight: Double?
)

object UserProfiles: IntIdTable() {
    val login = varchar("login", 256).uniqueIndex()
    val username = varchar("username", 256).nullable()
    val age = integer("age").nullable()
    val height = double("height").nullable()
    val weight = double("weight").nullable()
}

fun ResultRow.toUserProfileDataModel(): UserProfileModel {
    return UserProfileModel(
        login = this[UserProfiles.login],
        username = this[UserProfiles.username],
        age = this[UserProfiles.age],
        height = this[UserProfiles.height],
        weight = this[UserProfiles.weight]
    )
}