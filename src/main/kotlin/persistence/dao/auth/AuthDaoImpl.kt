package pro.aibar.sweatsketch.persistence.dao.auth

import api.auth.api.auth.Installation
import persistence.table.UserSessionsTable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.entity.toUserSessionEntity
import pro.aibar.sweatsketch.database.DatabaseSingleton.dbQuery
import pro.aibar.sweatsketch.persistence.table.UsersTable
import java.util.UUID


class AuthDaoImpl: AuthDao {
    override suspend fun validateUserAndGetId(login: String, passwordHash: String): UUID? = dbQuery {
        UsersTable
            .select(UsersTable.id)
            .where {
                (UsersTable.login eq login) and (UsersTable.passwordHash eq passwordHash)
            }
            .singleOrNull()
            ?.getOrNull(UsersTable.id)
            ?.value
    }

    override suspend fun addOrUpdateUserSession(
        userId: UUID,
        installation: Installation,
        maxInstallations: Int
    ) {
        transaction {
            val existingSession = UserSessionsTable
                .selectAll()
                .where { UserSessionsTable.user eq userId }
                .singleOrNull()

            if (existingSession != null) {
                val sessionData = existingSession.toUserSessionEntity()
                var updatedInstallations = sessionData.installations.filter { it.deviceId != installation.deviceId } + installation

                if (updatedInstallations.size > maxInstallations) {
                   updatedInstallations = updatedInstallations.drop(1)
                }

                UserSessionsTable.update({ UserSessionsTable.user eq userId }) {
                    it[installations] = Json.encodeToString(updatedInstallations)
                }
            } else {
                val newInstallations = listOf(installation)
                UserSessionsTable.insert {
                    it[user] = userId
                    it[installations] = Json.encodeToString(newInstallations)
                }
            }
        }
    }

    override suspend fun validateUserSession(
        userId: UUID,
        deviceId: String,
        refreshToken: String
    ): Boolean = dbQuery {
        val sessionRow = UserSessionsTable.selectAll().where {
            UserSessionsTable.user eq userId
        }.singleOrNull() ?: return@dbQuery false

        val sessionData = sessionRow.toUserSessionEntity()
        return@dbQuery sessionData.installations.any { it.deviceId == deviceId && it.refreshToken == refreshToken }
    }

    override suspend fun updateRefreshToken(
        userId: UUID,
        deviceId: String,
        newRefreshToken: String
    ) {
        transaction {
            val existingUser = UserSessionsTable.selectAll().where { UserSessionsTable.user eq userId }.singleOrNull()
            if (existingUser != null) {
                val sessionData = existingUser.toUserSessionEntity()
                val updatedInstallations = sessionData.installations.map {
                    if (it.deviceId == deviceId) it.copy(refreshToken = newRefreshToken) else it
                }
                UserSessionsTable.update({ UserSessionsTable.user eq userId }) {
                    it[installations] = Json.encodeToString(updatedInstallations)
                }
            }
        }
    }

    override suspend fun deleteSession(userId: UUID, deviceId: String) {
        transaction {
            val existingUser = UserSessionsTable.selectAll().where { UserSessionsTable.user eq userId }.singleOrNull()
            if (existingUser != null) {
                val sessionData = existingUser.toUserSessionEntity()
                val updatedInstallations = sessionData.installations.filter { it.deviceId != deviceId }

                if (updatedInstallations.isEmpty()) {
                    UserSessionsTable.deleteWhere { UserSessionsTable.user eq userId }
                } else {
                    UserSessionsTable.update({ UserSessionsTable.user eq userId }) {
                        it[installations] = Json.encodeToString(updatedInstallations)
                    }
                }
            }
        }
    }

    override suspend fun deleteAllSessions(userId: UUID) {
        transaction {
            UserSessionsTable.deleteWhere { UserSessionsTable.user eq userId }
        }
    }
}