package pro.aibar.sweatsketch.dao

import pro.aibar.sweatsketch.models.RefreshTokenModel

interface AuthDAOFacade {
    suspend fun validateUser(login: String, passwordHash: String): Boolean
    suspend fun addRefreshToken(refreshTokenModel: RefreshTokenModel)
    suspend fun validateRefreshToken(refreshTokenModel: RefreshTokenModel): Boolean
}