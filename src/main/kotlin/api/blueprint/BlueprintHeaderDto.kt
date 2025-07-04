package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.Serializable

@Serializable
data class BlueprintHeaderDto(
    val uuid: String,
    val name: String,
    val authorId: String,
    val visibility: BlueprintVisibility
)