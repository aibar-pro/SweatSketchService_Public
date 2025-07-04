package pro.aibar.sweatsketch.persistence.table.actiontemplates

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import pro.aibar.sweatsketch.persistence.table.workout.ExerciseBlueprintsTable

object DistanceActionBlueprintsTable : UUIDTable("distance_action_blueprints") {
    val exerciseBlueprint =
        reference("exercise_blueprint_uuid", ExerciseBlueprintsTable, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val sets = integer("sets")
    val min = double("min")
    val max = double("max").nullable()
    val unit = varchar("unit", 256)
    val isMax = bool("is_max")
}