package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.Serializable

@Serializable
data class TemplateHeaderDto(
    val uuid: String,
    val name: String,
    val authorId: String,
    val visibility: TemplateVisibility
)