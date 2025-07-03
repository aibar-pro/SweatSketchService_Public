package persistence.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import pro.aibar.sweatsketch.persistence.table.UsersTable

object UserProfilesTable : IntIdTable("user_profiles") {
    val user = reference("user_uuid", UsersTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val username = varchar("username", 256).nullable()
    val age = integer("age").nullable()
    val height = double("height").nullable()
    val weight = double("weight").nullable()
}