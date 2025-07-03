package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionTemplateDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.TimedActionTemplatesTable

fun ResultRow.toTimedActionDto(): ActionTemplateDto.Timed {
    return ActionTemplateDto.Timed(
        uuid = this[TimedActionTemplatesTable.id].value.toString(),
        position = this[TimedActionTemplatesTable.position],
        sets = this[TimedActionTemplatesTable.sets],
        min = this[TimedActionTemplatesTable.min],
        max = this[TimedActionTemplatesTable.max],
        isMax = this[TimedActionTemplatesTable.isMax]
    )
}