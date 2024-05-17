import pro.aibar.sweatsketch.models.UserDataModel
import pro.aibar.sweatsketch.models.UserProfileModel

interface UserDAOFacade {
    suspend fun addNewUser(login: String, passwordHash: String): UserDataModel?
    suspend fun addUserProfile(userProfile: UserProfileModel)
    suspend fun getUserProfile(login: String): UserProfileModel?
    suspend fun updateUserProfile(login: String, userProfileUpdate: UserProfileModel): UserProfileModel?
}