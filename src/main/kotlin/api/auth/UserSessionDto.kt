package api.auth.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class Installation(
    val deviceId: String,
    val userAgent: String,
    val host: String,
    val refreshToken: String
)

@Serializable
data class UserSessionDto(
    val user: String,
    val installations: List<Installation>
)