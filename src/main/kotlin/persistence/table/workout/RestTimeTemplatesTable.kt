package pro.aibar.sweatsketch.persistence.table.workout

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object RestTimeTemplatesTable : UUIDTable("rest_time_templates") {
    val workoutTemplate =
        reference("workout_template_uuid", WorkoutTemplatesTable, onDelete = ReferenceOption.CASCADE)
    val followingExerciseTemplate =
        reference("exercise_template_uuid", ExerciseTemplatesTable, onDelete = ReferenceOption.CASCADE)
    val isDefault = bool("isDefault")
    val duration = integer("duration")
}