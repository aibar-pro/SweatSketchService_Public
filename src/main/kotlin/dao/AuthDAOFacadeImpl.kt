package pro.aibar.sweatsketch.dao

import Installation
import UserSessions
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import pro.aibar.sweatsketch.database.DatabaseSingleton.dbQuery
import pro.aibar.sweatsketch.models.Users
import toUserSessionDataModel


class AuthDAOFacadeImpl: AuthDAOFacade {
    override suspend fun validateUser(login: String, passwordHash: String): Boolean = dbQuery {
        Users.select {
            (Users.login eq login) and (Users.passwordHash eq passwordHash)
        }.singleOrNull() != null
    }

    override suspend fun addOrUpdateUserSession(
        userLogin: String,
        installation: Installation,
        maxInstallations: Int
    ) {
        transaction {
            val existingSession = UserSessions.select { UserSessions.login eq userLogin }.singleOrNull()
            if (existingSession != null) {
                val sessionData = existingSession.toUserSessionDataModel()
                var updatedInstallations = sessionData.installations.filter { it.deviceId != installation.deviceId } + installation

                if (updatedInstallations.size > maxInstallations) {
                   updatedInstallations = updatedInstallations.drop(1)
                }

                UserSessions.update({ UserSessions.login eq userLogin }) {
                    it[installations] = Json.encodeToString(updatedInstallations)
                }
            } else {
                val newInstallations = listOf(installation)
                UserSessions.insert {
                    it[login] = userLogin
                    it[installations] = Json.encodeToString(newInstallations)
                }
            }
        }
    }

    override suspend fun validateUserSession(
        login: String,
        deviceId: String,
        refreshToken: String
    ): Boolean = dbQuery {
        val sessionRow = UserSessions.select {
            UserSessions.login eq login
        }.singleOrNull() ?: return@dbQuery false

        val sessionData = sessionRow.toUserSessionDataModel()
        return@dbQuery sessionData.installations.any { it.deviceId == deviceId && it.refreshToken == refreshToken }
    }

    override suspend fun updateRefreshToken(login: String, deviceId: String, newRefreshToken: String) {
        transaction {
            val existingUser = UserSessions.select { UserSessions.login eq login }.singleOrNull()
            if (existingUser != null) {
                val sessionData = existingUser.toUserSessionDataModel()
                val updatedInstallations = sessionData.installations.map {
                    if (it.deviceId == deviceId) it.copy(refreshToken = newRefreshToken) else it
                }
                UserSessions.update({ UserSessions.login eq login }) {
                    it[installations] = Json.encodeToString(updatedInstallations)
                }
            }
        }
    }

    override suspend fun deleteSessionByDeviceId(login: String, deviceId: String) {
        transaction {
            val existingUser = UserSessions.select { UserSessions.login eq login }.singleOrNull()
            if (existingUser != null) {
                val sessionData = existingUser.toUserSessionDataModel()
                val updatedInstallations = sessionData.installations.filter { it.deviceId != deviceId }

                if (updatedInstallations.isEmpty()) {
                    UserSessions.deleteWhere { UserSessions.login eq login }
                } else {
                    UserSessions.update({ UserSessions.login eq login }) {
                        it[installations] = Json.encodeToString(updatedInstallations)
                    }
                }
            }
        }
    }

    override suspend fun terminateAllSessionsForUser(login: String) {
        transaction {
            UserSessions.deleteWhere { UserSessions.login eq login }
        }
    }
}