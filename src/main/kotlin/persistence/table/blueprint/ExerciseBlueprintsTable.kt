package pro.aibar.sweatsketch.persistence.table.workout

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object ExerciseBlueprintsTable : UUIDTable("exercise_blueprints") {
    val workoutBlueprint =
        reference("workout_blueprint_uuid", WorkoutBlueprintsTable, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val position = integer("position")
    val superSets = integer("super_sets")
    val internalRest = integer("internal_rest").nullable()
    val restAfter = integer("rest_after").nullable()
}