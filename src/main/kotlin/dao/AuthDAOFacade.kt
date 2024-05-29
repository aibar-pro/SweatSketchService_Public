package pro.aibar.sweatsketch.dao

import pro.aibar.sweatsketch.models.RefreshTokenModel

interface AuthDAOFacade {
    suspend fun validateUser(login: String, passwordHash: String): Boolean
    suspend fun addRefreshToken(userLogin: String, refreshTokenModel: RefreshTokenModel)
    suspend fun updateRefreshToken(userLogin: String, newRefreshToken: RefreshTokenModel, oldRefreshToken: RefreshTokenModel)
    suspend fun validateRefreshToken(login: String, refreshTokenModel: RefreshTokenModel): Boolean
}