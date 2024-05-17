package pro.aibar.sweatsketch.dao

import UserDAOFacade
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import pro.aibar.sweatsketch.database.DatabaseSingleton.dbQuery
import pro.aibar.sweatsketch.models.*

class UserDAOFacadeImpl: UserDAOFacade {
    override suspend fun addNewUser(login: String, passwordHash: String): UserDataModel? = dbQuery {
        try {
            val insertStatement = Users.insert {
                it[Users.login] = login
                it[Users.passwordHash] = passwordHash
            }
            insertStatement.resultedValues?.singleOrNull()?.toUserDataModel()
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

    override suspend fun addUserProfile(userProfile: UserProfileModel) {
        dbQuery {
            UserProfiles.insert {
                it[login] = userProfile.login
                it[username] = userProfile.username
                it[age] = userProfile.age
                it[height] = userProfile.height
                it[weight] = userProfile.weight
            }
        }
    }

    override suspend fun getUserProfile(login: String): UserProfileModel? = dbQuery {
        UserProfiles.select {
            UserProfiles.login eq login
        }.mapNotNull { it.toUserProfileDataModel() }
            .singleOrNull()
    }

    override suspend fun updateUserProfile(
        login: String,
        userProfileUpdate: UserProfileModel
    ): UserProfileModel? = dbQuery {
        val updateResult = UserProfiles.update({ UserProfiles.login eq login }) {
            it[username] = userProfileUpdate.username
            it[age] = userProfileUpdate.age
            it[height] = userProfileUpdate.height
            it[weight] = userProfileUpdate.weight
        }
        if (updateResult > 0) {
            getUserProfile(login)
        } else {
            null
        }
    }
}
