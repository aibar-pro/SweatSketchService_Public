package pro.aibar.sweatsketch.persistence.table.actiontemplates

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import pro.aibar.sweatsketch.persistence.table.workout.ExerciseBlueprintsTable

object TimedActionBlueprintsTable : UUIDTable("timed_action_blueprints") {
    val exerciseBlueprint =
        reference("exercise_blueprint_uuid", ExerciseBlueprintsTable, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val sets = integer("sets")
    val min = integer("min")
    val max = integer("max").nullable()
    val isMax = bool("is_max")
}