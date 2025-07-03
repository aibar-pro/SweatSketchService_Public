package pro.aibar.sweatsketch.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentialDto(
    val login: String,
    val password: String
)
