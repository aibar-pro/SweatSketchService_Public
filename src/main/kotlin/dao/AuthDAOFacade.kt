package pro.aibar.sweatsketch.dao

import Installation

interface AuthDAOFacade {
    suspend fun validateUser(login: String, passwordHash: String): Boolean
    suspend fun addOrUpdateUserSession(
        userLogin: String,
        installation: Installation,
        maxInstallations: Int
    )
    suspend fun validateUserSession(
        login: String,
        deviceId: String,
        refreshToken: String
    ): Boolean
    suspend fun updateRefreshToken(login: String, deviceId: String, newRefreshToken: String)
    suspend fun deleteSessionByDeviceId(login: String, deviceId: String)
    suspend fun terminateAllSessionsForUser(login: String)
}