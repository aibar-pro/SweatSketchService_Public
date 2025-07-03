package persistence.entity

import api.auth.api.auth.UserSessionDto
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import persistence.table.UserSessionsTable

fun ResultRow.toUserSessionEntity(): UserSessionDto {
    return UserSessionDto(
        user = this[UserSessionsTable.user].value.toString(),
        installations = Json.Default.decodeFromString(this[UserSessionsTable.installations])
    )
}