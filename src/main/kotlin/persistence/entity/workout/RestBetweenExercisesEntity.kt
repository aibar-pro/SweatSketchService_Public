package pro.aibar.sweatsketch.persistence.entity.workout

import org.jetbrains.exposed.sql.ResultRow
import pro.aibar.sweatsketch.api.workout.RestTimeTemplateDto
import pro.aibar.sweatsketch.persistence.table.workout.RestTimeTemplatesTable

fun ResultRow.toRestTimeTemplateDto(): RestTimeTemplateDto {
    return RestTimeTemplateDto(
        followingExerciseTemplateId = this[RestTimeTemplatesTable.followingExerciseTemplate].value.toString(),
        isDefault = this[RestTimeTemplatesTable.isDefault],
        duration = this[RestTimeTemplatesTable.duration]
    )
}