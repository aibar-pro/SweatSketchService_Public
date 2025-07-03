package pro.aibar.sweatsketch.persistence.table.workout

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object ExerciseTemplatesTable : UUIDTable("exercise_templates") {
    val workoutTemplate =
        reference("workout_template_uuid", WorkoutTemplatesTable, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val position = integer("position")
    val superSets = integer("super_sets")
}