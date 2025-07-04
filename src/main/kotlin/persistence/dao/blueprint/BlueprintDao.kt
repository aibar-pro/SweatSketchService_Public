package pro.aibar.sweatsketch.persistence.dao.workout

import pro.aibar.sweatsketch.api.workout.BlueprintHeaderDto
import pro.aibar.sweatsketch.api.workout.BlueprintVisibility
import pro.aibar.sweatsketch.api.workout.WorkoutBlueprintDto
import java.util.UUID

interface BlueprintDao {
    fun saveBlueprint(
        blueprint: WorkoutBlueprintDto,
        visibility: BlueprintVisibility,
        authorId: UUID
    ): UUID

    fun listTemplates(
        visibility: BlueprintVisibility? = null,
        authorId: UUID? = null
    ): List<BlueprintHeaderDto>

    fun fetchBlueprint(id: UUID): WorkoutBlueprintDto?
}