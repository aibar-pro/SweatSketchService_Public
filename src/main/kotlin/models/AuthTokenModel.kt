package pro.aibar.sweatsketch.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenModel(val accessToken: String, val refreshToken: String, val expiresIn: ULong)