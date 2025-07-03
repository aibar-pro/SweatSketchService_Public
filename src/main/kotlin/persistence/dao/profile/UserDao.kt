import pro.aibar.sweatsketch.persistence.entity.UserDataModel
import pro.aibar.sweatsketch.api.profile.UserProfileDto
import java.util.UUID

interface UserDao {
    suspend fun addNewUser(login: String, passwordHash: String): UserDataModel?
    suspend fun addUserProfile(userId: UUID, userProfile: UserProfileDto)
    suspend fun getUserProfile(userId: UUID): UserProfileDto?
    suspend fun updateUserProfile(userId: UUID, userProfileUpdate: UserProfileDto): UserProfileDto?
}