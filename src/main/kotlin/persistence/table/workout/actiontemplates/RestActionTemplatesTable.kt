package pro.aibar.sweatsketch.persistence.table.actiontemplates

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import pro.aibar.sweatsketch.persistence.table.workout.ExerciseTemplatesTable

object RestActionTemplatesTable : UUIDTable("rest_action_templates") {
    val exerciseTemplate =
        reference("exercise_template_uuid", ExerciseTemplatesTable, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val duration = integer("duration")
}