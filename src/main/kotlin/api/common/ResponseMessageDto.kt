package pro.aibar.sweatsketch.api.common

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMessageDto(
    val message: String
)