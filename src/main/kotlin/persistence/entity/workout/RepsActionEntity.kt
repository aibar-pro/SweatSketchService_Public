package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionTemplateDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.RepsActionTemplatesTable

fun ResultRow.toRepsActionDto(): ActionTemplateDto.Reps {
    return ActionTemplateDto.Reps(
        uuid = this[RepsActionTemplatesTable.id].value.toString(),
        position = this[RepsActionTemplatesTable.position],
        sets = this[RepsActionTemplatesTable.sets],
        min = this[RepsActionTemplatesTable.min],
        max = this[RepsActionTemplatesTable.max],
        isMax = this[RepsActionTemplatesTable.isMax]
    )
}