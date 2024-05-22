package pro.aibar.sweatsketch.models

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentialModel(val login: String, val password: String)