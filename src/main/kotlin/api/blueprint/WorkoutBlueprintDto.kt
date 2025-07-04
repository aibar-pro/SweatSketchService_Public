package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pro.aibar.sweatsketch.api.common.ApiConstants

@Serializable
data class WorkoutBlueprintDto(
    val schemaVersion: Int = ApiConstants.WORKOUT_SCHEMA_VERSION,
    val templateId: String? = null,
    val name: String,
    val exercises: List<ExerciseBlueprintDto>,
    val defaultRest: Int? = null
)

@Serializable
data class ExerciseBlueprintDto(
    val position: Int,
    val name: String,
    val superSets: Int,
    val actions: List<ActionBlueprintDto>,
    val internalRest: Int? = null,
    val restAfter: Int? = null
)

@Serializable
sealed class ActionBlueprintDto {
    @Serializable @SerialName("reps")
    data class Reps(
        val position: Int,
        val sets: Int,
        val min: Int,
        val max: Int? = null,
        val isMax: Boolean
    ) : ActionBlueprintDto()

    @Serializable @SerialName("timed")
    data class Timed(
        val position: Int,
        val sets: Int,
        val min: Int,
        val max: Int? = null,
        val isMax: Boolean
    ) : ActionBlueprintDto()

    @Serializable @SerialName("distance")
    data class Distance(
        val position: Int,
        val sets: Int,
        val min: Double,
        val max: Double? = null,
        val unit: String,
        val isMax: Boolean
    ) : ActionBlueprintDto()
}