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
import pro.aibar.sweatsketch.models.Users
import toRefreshTokenDataModel


class AuthDAOFacadeImpl: AuthDAOFacade {
    override suspend fun validateUser(login: String, passwordHash: String): Boolean = dbQuery {
        Users.select {
            (Users.login eq login) and (Users.passwordHash eq passwordHash)
        }.singleOrNull() != null
    }

    override suspend fun addRefreshToken(refreshTokenModel: RefreshTokenModel) {
        transaction {
            val existingUser = RefreshTokens.select { RefreshTokens.login eq refreshTokenModel.login }.singleOrNull()
            if (existingUser != null) {
                val refreshTokenData = existingUser.toRefreshTokenDataModel()
                val updatedTokens = refreshTokenData.refreshTokensMap + (System.currentTimeMillis() to refreshTokenModel.refreshToken)
                RefreshTokens.update({ RefreshTokens.login eq refreshTokenModel.login }) {
                    it[refreshTokensMap] = Json.encodeToString(updatedTokens)
                }
            } else {
                val newTokens = mapOf(System.currentTimeMillis() to refreshTokenModel.refreshToken)
                RefreshTokens.insert {
                    it[login] = refreshTokenModel.login
                    it[refreshTokensMap] = Json.encodeToString(newTokens)
                }
            }
        }
    }

    override suspend fun validateRefreshToken(refreshTokenModel: RefreshTokenModel): Boolean = dbQuery {
        val refreshTokenRow = RefreshTokens.select {
            RefreshTokens.login eq refreshTokenModel.login
        }.singleOrNull() ?: return@dbQuery false

        val refreshTokensMap: Map<Long, String> = Json.decodeFromString(refreshTokenRow[RefreshTokens.refreshTokensMap])
        return@dbQuery refreshTokensMap.containsValue(refreshTokenModel.refreshToken)
    }
}
