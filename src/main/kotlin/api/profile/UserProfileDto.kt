package pro.aibar.sweatsketch.api.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val username: String?,
    val age: Int?,
    val height: Double?,
    val weight: Double?
)