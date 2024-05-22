import io.ktor.application.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import kotlin.test.*

class UserDAOFacadeImplTest {
    private lateinit var db: Database
    private lateinit var userDAO: UserDAOFacadeImpl
    private val jwtSecret = "secret"
    private val jwtIssuer = "issuer"
    private val jwtAudience = "audience"

    @BeforeEach
    fun setup() {
        db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.create(UserProfiles, RefreshTokens, Users)
        }
        userDAO = UserDAOFacadeImpl()
    }

    @AfterEach
    fun teardown() {
        transaction(db) {
            SchemaUtils.drop(UserProfiles, RefreshTokens, Users)
        }
    }

    @Test
    fun testUserCreation() = runBlocking {
        // Create user
        val newUser = UserCredentialModel("testUser", "password")
        val passwordHash = hashPassword(newUser.password)
        val user = userDAO.addNewUser(newUser.login, passwordHash)
        assertNotNull(user, "User should be created successfully")

        // Try to create duplicate user
        val duplicateUser = userDAO.addNewUser(newUser.login, passwordHash)
        assertNull(duplicateUser, "Duplicate user creation should fail")
    }

    @Test
    fun testUserLogin() = runBlocking {
        // Create user
        val newUser = UserCredentialModel("testUser", "password")
        val passwordHash = hashPassword(newUser.password)
        userDAO.addNewUser(newUser.login, passwordHash)

        // Invalid login
        val invalidLogin = userDAO.validateUser("testUser", "wrongPassword")
        assertFalse(invalidLogin, "Invalid login should fail")

        // Valid login
        val validLogin = userDAO.validateUser("testUser", "password")
        assertTrue(validLogin, "Valid login should succeed")
    }

    @Test
    fun testAuthRefreshToken() = runBlocking {
        // Create user
        val newUser = UserCredentialModel("testUser", "password")
        val passwordHash = hashPassword(newUser.password)
        userDAO.addNewUser(newUser.login, passwordHash)

        // Add refresh token
        val refreshTokenModel = RefreshTokenModel("testUser", "refreshToken")
        userDAO.addRefreshToken(refreshTokenModel)

        // Validate refresh token
        val validToken = userDAO.validateRefreshToken(refreshTokenModel)
        assertTrue(validToken, "Valid refresh token should succeed")

        // Wrong login
        val invalidLoginToken = userDAO.validateRefreshToken(RefreshTokenModel("wrongUser", "refreshToken"))
        assertFalse(invalidLoginToken, "Validation with wrong login should fail")

        // Wrong refresh token
        val invalidRefreshToken = userDAO.validateRefreshToken(RefreshTokenModel("testUser", "wrongToken"))
        assertFalse(invalidRefreshToken, "Validation with wrong refresh token should fail")
    }

    @Test
    fun testUserProfile() = runBlocking {
        // Create user
        val newUser = UserCredentialModel("testUser", "password")
        val passwordHash = hashPassword(newUser.password)
        userDAO.addNewUser(newUser.login, passwordHash)

        // Generate access token
        val accessToken = generateToken("testUser", jwtSecret, jwtIssuer, jwtAudience)

        // Test create profile with wrong token (use refresh token instead of access)
        val refreshToken = generateRefreshToken("testUser", jwtSecret, jwtIssuer, jwtAudience)
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Post, "/user/profile") {
                addHeader(HttpHeaders.Authorization, "Bearer $refreshToken")
                setBody("""{"login":"testUser","username":"testUser","age":30,"height":180.5,"weight":75.0}""")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status(), "Profile creation with wrong token should fail")
            }
        }

        // Test create profile with wrong token (made up token)
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Post, "/user/profile") {
                addHeader(HttpHeaders.Authorization, "Bearer madeUpToken")
                setBody("""{"login":"testUser","username":"testUser","age":30,"height":180.5,"weight":75.0}""")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status(), "Profile creation with made-up token should fail")
            }
        }

        // Successful profile creation
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Post, "/user/profile") {
                addHeader(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody("""{"login":"testUser","username":"testUser","age":30,"height":180.5,"weight":75.0}""")
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status(), "Profile creation with valid token should succeed")
            }
        }

        // Test get profile with wrong token
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/user/profile/testUser") {
                addHeader(HttpHeaders.Authorization, "Bearer madeUpToken")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status(), "Get profile with wrong token should fail")
            }
        }

        // Successful get profile
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/user/profile/testUser") {
                addHeader(HttpHeaders.Authorization, "Bearer $accessToken")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status(), "Get profile with valid token should succeed")
                assertEquals("""{"login":"testUser","username":"testUser","age":30,"height":180.5,"weight":75.0}""", response.content, "Profile content should match")
            }
        }

        // Test update profile with wrong token
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Put, "/user/profile/testUser") {
                addHeader(HttpHeaders.Authorization, "Bearer madeUpToken")
                setBody("""{"login":"testUser","username":"updatedUser","age":35,"height":185.5,"weight":80.0}""")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status(), "Update profile with wrong token should fail")
            }
        }

        // Successful update profile
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Put, "/user/profile/testUser") {
                addHeader(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody("""{"login":"testUser","username":"updatedUser","age":35,"height":185.5,"weight":80.0}""")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status(), "Update profile with valid token should succeed")
            }

            // Verify updated profile
            handleRequest(HttpMethod.Get, "/user/profile/testUser") {
                addHeader(HttpHeaders.Authorization, "Bearer $accessToken")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status(), "Get updated profile with valid token should succeed")
                assertEquals("""{"login":"testUser","username":"updatedUser","age":35,"height":185.5,"weight":80.0}""", response.content, "Updated profile content should match")
            }
        }
    }

    // Utility function to generate JWT tokens
    private fun generateToken(login: String, secret: String, issuer: String, audience: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("login", login)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000)) // 1 minute expiry
            .sign(Algorithm.HMAC256(secret))
    }

    private fun generateRefreshToken(login: String, secret: String, issuer: String, audience: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("login", login)
            .withExpiresAt(Date(System.currentTimeMillis() + 120000)) // 2 minutes expiry
            .sign(Algorithm.HMAC256(secret))
    }

    // Utility function to hash passwords
    private fun hashPassword(password: String): String {
        // Implement password hashing logic (e.g., using BCrypt)
        return password // Replace with actual hashing
    }

    private fun Application.module(testing: Boolean = true) {
        configureAuth()
        routing {
            userRoutes(userDAO)
        }
    }
}
