package pro.aibar.sweatsketch.persistence.dao.profile

import UserDao
import pro.aibar.sweatsketch.api.profile.UserProfileDto
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import persistence.table.UserProfilesTable
import pro.aibar.sweatsketch.database.DatabaseSingleton.dbQuery
import pro.aibar.sweatsketch.persistence.entity.UserDataModel
import pro.aibar.sweatsketch.persistence.entity.toUserDataEntity
import pro.aibar.sweatsketch.persistence.entity.toUserProfileEntity
import pro.aibar.sweatsketch.persistence.table.UsersTable
import java.util.UUID

class UserDaoImpl: UserDao {
    override suspend fun addNewUser(login: String, passwordHash: String): UserDataModel? = dbQuery {
        try {
            val insertStatement = UsersTable.insert {
                it[UsersTable.login] = login
                it[UsersTable.passwordHash] = passwordHash
            }
            insertStatement.resultedValues?.singleOrNull()?.toUserDataEntity()
        } catch (e: Exception) {
            when (e) {
                is ExposedSQLException -> {
                    if (e.message?.contains("USERS_LOGIN_UNIQUE") == true) {
                        null  // Username already exists
                    } else {
                        throw e  // Re-throw the exception if it's not related to our unique constraint
                    }
                }
                else -> throw e
            }
        }
    }

    override suspend fun addUserProfile(userId: UUID, userProfile: UserProfileDto): Unit = dbQuery {
        UserProfilesTable.insert {
            it[user] = userId
            it[username] = userProfile.username
            it[age] = userProfile.age
            it[height] = userProfile.height
            it[weight] = userProfile.weight
        }
    }

    override suspend fun getUserProfile(userId: UUID): UserProfileDto? = dbQuery {
        UserProfilesTable
            .selectAll()
            .where {
                UserProfilesTable.user eq userId
            }
            .mapNotNull { it.toUserProfileEntity() }
            .singleOrNull()
    }

    override suspend fun updateUserProfile(
        userId: UUID,
        userProfileUpdate: UserProfileDto
    ): UserProfileDto? = dbQuery {
        val updateResult = UserProfilesTable.update({ UserProfilesTable.user eq userId }) {
            it[username] = userProfileUpdate.username
            it[age] = userProfileUpdate.age
            it[height] = userProfileUpdate.height
            it[weight] = userProfileUpdate.weight
        }
        if (updateResult > 0) {
            getUserProfile(userId)
        } else {
            null
        }
    }
}
