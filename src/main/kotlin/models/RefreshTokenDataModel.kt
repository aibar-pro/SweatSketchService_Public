import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

data class RefreshTokenDataModel(val login: String, val refreshTokensMap: Map<Long, String>)

object RefreshTokens: IntIdTable() {
    val login = varchar("login", 256).uniqueIndex()
    val refreshTokensMap = text("refreshTokensMap")
}

fun ResultRow.toRefreshTokenDataModel(): RefreshTokenDataModel {
    return RefreshTokenDataModel(
        login = this[RefreshTokens.login],
        refreshTokensMap = Json.decodeFromString(this[RefreshTokens.refreshTokensMap])
    )
}