package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.ActionTemplateDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.RestActionTemplatesTable

fun ResultRow.toRestActionDto(): ActionTemplateDto.Rest {
    return ActionTemplateDto.Rest(
        uuid = this[RestActionTemplatesTable.id].value.toString(),
        position = this[RestActionTemplatesTable.position],
        duration = this[RestActionTemplatesTable.duration]
    )
}