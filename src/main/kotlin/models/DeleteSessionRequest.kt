package pro.aibar.sweatsketch.models

import kotlinx.serialization.Serializable

@Serializable
data class DeleteSessionRequest(val login: String)