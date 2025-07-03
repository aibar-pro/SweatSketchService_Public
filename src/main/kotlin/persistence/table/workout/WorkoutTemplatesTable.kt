package pro.aibar.sweatsketch.persistence.table.workout

import org.jetbrains.exposed.dao.id.UUIDTable
import pro.aibar.sweatsketch.api.workout.TemplateVisibility

object WorkoutTemplatesTable : UUIDTable("workout_templates") {
    val name = varchar("name", 255)
    val authorId = uuid("author_id")
    val visibility = enumerationByName("visibility", 16, TemplateVisibility::class)
//    val createdAt = timestamp("created_at")
}