package persistence.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import pro.aibar.sweatsketch.persistence.table.UsersTable

object UserSessionsTable : IntIdTable("user_sessions") {
    val user = reference("user_uuid", UsersTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val installations = text("installations")
}

