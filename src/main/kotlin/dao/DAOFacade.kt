package pro.aibar.sweatsketch.dao

import pro.aibar.sweatsketch.models.User

interface DAOFacade {
    suspend fun addNewUser(username: String, passwordHash: String): User?
    suspend fun validateUser(username: String, passwordHash: String): Boolean
}