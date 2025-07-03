package pro.aibar.sweatsketch.persistence.dao.auth

import api.auth.api.auth.Installation
import java.util.UUID

interface AuthDao {
    suspend fun validateUserAndGetId(
        login: String,
        passwordHash: String
    ): UUID?

    suspend fun addOrUpdateUserSession(
        userId: UUID,
        installation: Installation,
        maxInstallations: Int
    )

    suspend fun validateUserSession(
        userId: UUID,
        deviceId: String,
        refreshToken: String
    ): Boolean

    suspend fun updateRefreshToken(
        userId: UUID,
        deviceId: String,
        newRefreshToken: String
    )

    suspend fun deleteSession(
        userId: UUID,
        deviceId: String
    )

    suspend fun deleteAllSessions(
        userId: UUID
    )
}