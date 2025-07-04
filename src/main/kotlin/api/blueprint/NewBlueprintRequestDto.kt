package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.Serializable

@Serializable
data class NewBlueprintRequestDto(
    val blueprint: WorkoutBlueprintDto,
    val visibility: BlueprintVisibility = BlueprintVisibility.PRIVATE
)