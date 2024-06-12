import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class Installation(
    val deviceId: String,
    val userAgent: String,
    val host: String,
    val refreshToken: String
)

@Serializable
data class UserSessionDataModel(
    val login: String,
    val installations: List<Installation>
)

object UserSessions: IntIdTable() {
    val login = varchar("login", 256).uniqueIndex()
    val installations = text("installations")
}

fun ResultRow.toUserSessionDataModel(): UserSessionDataModel {
    return UserSessionDataModel(
        login = this[UserSessions.login],
        installations = Json.decodeFromString(this[UserSessions.installations])
    )
}