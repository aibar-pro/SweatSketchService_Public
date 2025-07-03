package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionTemplateDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.DistanceActionTemplatesTable

fun ResultRow.toDistanceActionDto(): ActionTemplateDto.Distance {
    return ActionTemplateDto.Distance(
        uuid = this[DistanceActionTemplatesTable.id].value.toString(),
        position = this[DistanceActionTemplatesTable.position],
        sets = this[DistanceActionTemplatesTable.sets],
        min = this[DistanceActionTemplatesTable.min],
        max = this[DistanceActionTemplatesTable.max],
        unit = this[DistanceActionTemplatesTable.unit],
        isMax = this[DistanceActionTemplatesTable.isMax]
    )
}