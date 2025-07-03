package pro.aibar.sweatsketch.persistence.entity

import pro.aibar.sweatsketch.api.profile.UserProfileDto
import org.jetbrains.exposed.sql.ResultRow
import persistence.table.UserProfilesTable

fun ResultRow.toUserProfileEntity(): UserProfileDto {
    return UserProfileDto(
        username = this[UserProfilesTable.username],
        age = this[UserProfilesTable.age],
        height = this[UserProfilesTable.height],
        weight = this[UserProfilesTable.weight]
    )
}