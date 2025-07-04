package pro.aibar.sweatsketch.persistence.table.workout

import org.jetbrains.exposed.dao.id.UUIDTable
import pro.aibar.sweatsketch.api.workout.BlueprintVisibility

object WorkoutBlueprintsTable : UUIDTable("workout_blueprints") {
    val name = varchar("name", 255)
    val authorId = uuid("author_id")
    val visibility = enumerationByName("visibility", 16, BlueprintVisibility::class)
    var defaultRest = integer("default_rest").nullable()
//    val createdAt = timestamp("created_at")
}