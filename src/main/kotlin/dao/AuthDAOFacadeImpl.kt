package pro.aibar.sweatsketch.dao

import RefreshTokens
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import pro.aibar.sweatsketch.database.DatabaseSingleton.dbQuery
import pro.aibar.sweatsketch.models.RefreshTokenModel
import pro.aibar.sweatsketch.models.TokenNotFoundException
import pro.aibar.sweatsketch.models.Users
import toRefreshTokenDataModel


class AuthDAOFacadeImpl: AuthDAOFacade {
    override suspend fun validateUser(login: String, passwordHash: String): Boolean = dbQuery {
        Users.select {
            (Users.login eq login) and (Users.passwordHash eq passwordHash)
        }.singleOrNull() != null
    }

    override suspend fun addRefreshToken(userLogin: String, refreshTokenModel: RefreshTokenModel) {
        transaction {
            val existingUser = RefreshTokens.select { RefreshTokens.login eq userLogin }.singleOrNull()
            if (existingUser != null) {
                val refreshTokenData = existingUser.toRefreshTokenDataModel()
                val updatedTokens = refreshTokenData.refreshTokensMap + (System.currentTimeMillis() to refreshTokenModel.refreshToken)
                RefreshTokens.update({ RefreshTokens.login eq userLogin }) {
                    it[refreshTokensMap] = Json.encodeToString(updatedTokens)
                }
            } else {
                val newTokens = mapOf(System.currentTimeMillis() to refreshTokenModel.refreshToken)
                RefreshTokens.insert {
                    it[login] = userLogin
                    it[refreshTokensMap] = Json.encodeToString(newTokens)
                }
            }
        }
    }

    override suspend fun updateRefreshToken(
        userLogin: String,
        newRefreshToken: RefreshTokenModel,
        oldRefreshToken: RefreshTokenModel
    ) {
        transaction {
            val existingUser = RefreshTokens.select { RefreshTokens.login eq userLogin }.singleOrNull()
            if (existingUser != null) {
                val refreshTokenData = existingUser.toRefreshTokenDataModel()
                var oldTokenFound = false
                val updatedTokens = refreshTokenData.refreshTokensMap.mapNotNull {
                    if (it.value == oldRefreshToken.refreshToken) {
                        oldTokenFound = true
                        null
                    } else {
                        it.key to it.value
                    }
                }.toMap() + (System.currentTimeMillis() to newRefreshToken.refreshToken)

                if (!oldTokenFound) {
                    throw TokenNotFoundException("Old refresh token not found for user $userLogin")
                }

                RefreshTokens.update({ RefreshTokens.login eq userLogin }) {
                    it[refreshTokensMap] = Json.encodeToString(updatedTokens)
                }
            } else {
                throw TokenNotFoundException("User $userLogin not found")
            }
        }
    }

    override suspend fun validateRefreshToken(login: String, refreshTokenModel: RefreshTokenModel): Boolean = dbQuery {
        val refreshTokenRow = RefreshTokens.select {
            RefreshTokens.login eq login
        }.singleOrNull() ?: return@dbQuery false

        val refreshTokensMap: Map<Long, String> = Json.decodeFromString(refreshTokenRow[RefreshTokens.refreshTokensMap])
        return@dbQuery refreshTokensMap.containsValue(refreshTokenModel.refreshToken)
    }
}