package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionBlueprintDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.RepsActionBlueprintsTable

fun ResultRow.toRepsActionDto(): ActionBlueprintDto.Reps {
    return ActionBlueprintDto.Reps(
        position = this[RepsActionBlueprintsTable.position],
        sets = this[RepsActionBlueprintsTable.sets],
        min = this[RepsActionBlueprintsTable.min],
        max = this[RepsActionBlueprintsTable.max],
        isMax = this[RepsActionBlueprintsTable.isMax]
    )
}