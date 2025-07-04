package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionBlueprintDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.DistanceActionBlueprintsTable

fun ResultRow.toDistanceActionDto(): ActionBlueprintDto.Distance {
    return ActionBlueprintDto.Distance(
        position = this[DistanceActionBlueprintsTable.position],
        sets = this[DistanceActionBlueprintsTable.sets],
        min = this[DistanceActionBlueprintsTable.min],
        max = this[DistanceActionBlueprintsTable.max],
        unit = this[DistanceActionBlueprintsTable.unit],
        isMax = this[DistanceActionBlueprintsTable.isMax]
    )
}