package pro.aibar.sweatsketch.dao

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import pro.aibar.sweatsketch.dao.DatabaseSingleton.dbQuery
import pro.aibar.sweatsketch.models.User
import pro.aibar.sweatsketch.models.Users

class DAOFacadeImpl: DAOFacade {
    override suspend fun addNewUser(username: String, passwordHash: String): User? = dbQuery {
        try {
            val insertStatement = Users.insert {
                it[Users.username] = username
                it[Users.passwordHash] = passwordHash
            }
            insertStatement.resultedValues?.singleOrNull()?.let {
                toUser(it)
            }
        } catch (e: Exception) {
            when (e) {
                is ExposedSQLException -> {
                    if (e.message?.contains("USERS_USERNAME_UNIQUE") == true) {
                        null  // Username already exists
                    } else {
                        throw e  // Re-throw the exception if it's not related to our unique constraint
                    }
                }
                else -> throw e
            }
        }
    }

    private fun toUser(row: ResultRow): User =
        User(
            username = row[Users.username],
            passwordHash = row[Users.passwordHash]
        )

    override suspend fun validateUser(username: String, passwordHash: String): Boolean = dbQuery {
        Users.select {
            (Users.username eq username) and (Users.passwordHash eq passwordHash)
        }.singleOrNull() != null
    }

}