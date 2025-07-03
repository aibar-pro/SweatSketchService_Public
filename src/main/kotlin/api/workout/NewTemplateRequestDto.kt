package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.Serializable

@Serializable
data class NewTemplateRequestDto(
    val workout: WorkoutTemplateDto,
    val visibility: TemplateVisibility = TemplateVisibility.PRIVATE
)