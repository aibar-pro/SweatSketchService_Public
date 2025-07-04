package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionBlueprintDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.TimedActionBlueprintsTable

fun ResultRow.toTimedActionDto(): ActionBlueprintDto.Timed {
    return ActionBlueprintDto.Timed(
        position = this[TimedActionBlueprintsTable.position],
        sets = this[TimedActionBlueprintsTable.sets],
        min = this[TimedActionBlueprintsTable.min],
        max = this[TimedActionBlueprintsTable.max],
        isMax = this[TimedActionBlueprintsTable.isMax]
    )
}