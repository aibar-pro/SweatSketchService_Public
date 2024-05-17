package pro.aibar.sweatsketch.models

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenModel(
    val login: String,
    val refreshToken: String,
)
