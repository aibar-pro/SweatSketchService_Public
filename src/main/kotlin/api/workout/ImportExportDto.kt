package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pro.aibar.sweatsketch.api.common.ApiConstants

@Serializable
data class ImportExportWrapper(
    val schemaVersion: Int = ApiConstants.WORKOUT_SCHEMA_VERSION,
    val workout: WorkoutTemplateDto
)

@Serializable
data class WorkoutTemplateDto(
    val uuid: String,
    val name: String,
    val exercises: List<ExerciseTemplateDto>,
    val restBetween: List<RestTimeTemplateDto>
)

@Serializable
data class ExerciseTemplateDto(
    val uuid: String,
    val position: Int,
    val name: String,
    val superSets: Int,
    val actions: List<ActionTemplateDto>
)

@Serializable
sealed class ActionTemplateDto {
    @Serializable @SerialName("reps")
    data class Reps(
        val uuid: String,
        val position: Int,
        val sets: Int,
        val min: Int,
        val max: Int? = null,
        val isMax: Boolean
    ) : ActionTemplateDto()

    @Serializable @SerialName("timed")
    data class Timed(
        val uuid: String,
        val position: Int,
        val sets: Int,
        val min: Int,
        val max: Int? = null,
        val isMax: Boolean
    ) : ActionTemplateDto()

    @Serializable @SerialName("distance")
    data class Distance(
        val uuid: String,
        val position: Int,
        val sets: Int,
        val min: Double,
        val max: Double? = null,
        val unit: String,
        val isMax: Boolean
    ) : ActionTemplateDto()

    @Serializable @SerialName("rest")
    data class Rest(
        val uuid: String,
        val position: Int,
        val duration: Int
    ) : ActionTemplateDto()
}

@Serializable
data class RestTimeTemplateDto(
    val followingExerciseTemplateId: String,
    val isDefault: Boolean,
    val duration: Int
)