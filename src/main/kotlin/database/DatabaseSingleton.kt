package pro.aibar.sweatsketch.database

import persistence.table.UserSessionsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.table.UserProfilesTable
import pro.aibar.sweatsketch.persistence.table.UsersTable
import pro.aibar.sweatsketch.persistence.table.actiontemplates.DistanceActionBlueprintsTable
import pro.aibar.sweatsketch.persistence.table.actiontemplates.RepsActionBlueprintsTable
import pro.aibar.sweatsketch.persistence.table.actiontemplates.TimedActionBlueprintsTable
import pro.aibar.sweatsketch.persistence.table.workout.ExerciseBlueprintsTable
import pro.aibar.sweatsketch.persistence.table.workout.WorkoutBlueprintsTable

object DatabaseSingleton {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(UsersTable)
            SchemaUtils.create(UserSessionsTable)
            SchemaUtils.create(UserProfilesTable)
            SchemaUtils.create(
                WorkoutBlueprintsTable,
                ExerciseBlueprintsTable,
                RepsActionBlueprintsTable,
                TimedActionBlueprintsTable,
                DistanceActionBlueprintsTable
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}