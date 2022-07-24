package com.shykial.kScrapperCore.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScrapperCore.helper.Given
import com.shykial.kScrapperCore.helper.RestTest
import com.shykial.kScrapperCore.helper.Then
import com.shykial.kScrapperCore.helper.When
import com.shykial.kScrapperCore.helper.extractingBody
import com.shykial.kScrapperCore.helper.saveIn
import com.shykial.kScrapperCore.helpers.plusMinutes
import com.shykial.kScrapperCore.helpers.shouldBeWithin
import com.shykial.kScrapperCore.model.entity.ApplicationUser
import com.shykial.kScrapperCore.model.entity.UserRole
import com.shykial.kScrapperCore.repository.ApplicationUserRepository
import com.shykial.kScrapperCore.security.JwtProperties
import com.shykial.kScrapperCore.starter.MongoDBStarter
import generated.com.shykial.kScrapperCore.models.AuthToken
import generated.com.shykial.kScrapperCore.models.ErrorResponse
import generated.com.shykial.kScrapperCore.models.ErrorType
import generated.com.shykial.kScrapperCore.models.IdResponse
import generated.com.shykial.kScrapperCore.models.LoginRequest
import generated.com.shykial.kScrapperCore.models.RegisterUserRequest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.Instant

private const val AUTH_ENDPOINT = "/auth"

@SpringBootTest
internal class AuthControllerTest(
    override val objectMapper: ObjectMapper,
    override val webTestClient: WebTestClient,
    private val applicationUserRepository: ApplicationUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties
) : RestTest(), MongoDBStarter {

    init {
        RestAssuredWebTestClient.basePath = AUTH_ENDPOINT
    }

    @BeforeEach
    fun setup() {
        runBlocking { applicationUserRepository.deleteAll() }
    }

    @Nested
    inner class PositiveOutcome {
        @Test
        fun `should generate proper JWT for user on POST request`() = runTest {
            val rawPassword = "testPassword"
            val validUser = sampleValidUser(rawPassword).saveIn(applicationUserRepository)
            val loginRequest = LoginRequest(login = validUser.login, password = rawPassword)
            Given {
                jsonBody(loginRequest)
            } When {
                post("/login")
            } Then {
                status(HttpStatus.OK)
                extractingBody<AuthToken> {
                    JWT.decode(it.token).assertProperTokenGenerated(validUser)
                }
            }
        }

        @Test
        fun `should properly register new user on POST request and return its id`() = runTest {
            val request = RegisterUserRequest(
                login = "properLogin",
                email = "proper@email.com",
                password = "properPassword4812$"
            )
            applicationUserRepository.findAll().toList().shouldBeEmpty()

            Given {
                jsonBody(request)
            } When {
                post("/register")
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<IdResponse> { response ->
                    applicationUserRepository.findById(response.id)!!.assertProperUserPersisted(request)
                }
            }
        }
    }

    @Nested
    inner class NegativeOutcome {
        @Test
        fun `should return UNAUTHORIZED when logging in providing invalid credentials`() = runTest {
            val rawPassword = "testPassword2"
            val validUser = sampleValidUser(rawPassword).saveIn(applicationUserRepository)
            val invalidLoginRequest = LoginRequest(login = validUser.login, password = "invalidPassword")
            Given {
                jsonBody(invalidLoginRequest)
            } When {
                post("/login")
            } Then {
                status(HttpStatus.UNAUTHORIZED)
                extractingBody<ErrorResponse> {
                    it.errorType shouldBe ErrorType.AUTHORIZATION_FAILURE
                }
            }
        }

        @Test
        fun `should return BAD REQUEST when registering with invalid data`() = runTest {
            val invalidRequest = RegisterUserRequest(
                login = "validLogin",
                email = "invalidEmail",
                password = "validPassword123*"
            )

            Given {
                jsonBody(invalidRequest)
            } When {
                post("/register")
            } Then {
                status(HttpStatus.BAD_REQUEST)
                applicationUserRepository.findAll().toList().shouldBeEmpty()
            }
        }

        @Test
        fun `should fail registering new user with non-unique login`() = runTest {
            val existingUser = ApplicationUser(
                login = "existingLogin",
                passwordHash = "passwordHash",
                email = "email@email.com"
            ).saveIn(applicationUserRepository)
            val request = RegisterUserRequest(
                login = existingUser.login,
                email = "sampleEmail@email.com",
                password = "samplePassword123*"
            )

            Given {
                jsonBody(request)
            } When {
                post("/register")
            } Then {
                status(HttpStatus.CONFLICT)
                applicationUserRepository.findByLogin(existingUser.login) shouldBe existingUser
            }
        }

    }

    private fun ApplicationUser.assertProperUserPersisted(
        request: RegisterUserRequest
    ) {
        login shouldBe request.login
        login shouldBe request.login
        email shouldBe request.email
        passwordEncoder.matches(request.password, passwordHash).shouldBeTrue()
    }

    private fun sampleValidUser(rawPassword: String) = ApplicationUser(
        login = "testLogin",
        passwordHash = passwordEncoder.encode(rawPassword),
        email = "testEmail@testDomain.com",
        role = UserRole.API_USER,
        isDisabled = false
    )

    private fun DecodedJWT.assertProperTokenGenerated(validUser: ApplicationUser) {
        subject shouldBe validUser.login
        claims[jwtProperties.rolesClaimName]?.asList(String::class.java) shouldBe listOf(validUser.role.name)
        issuer shouldBe jwtProperties.issuer
        issuedAt.toInstant().shouldBeWithin(Duration.ofMinutes(1), Instant.now())
        expiresAt.toInstant().shouldBeWithin(
            margin = Duration.ofMinutes(1),
            other = Instant.now().plusMinutes(jwtProperties.validityInMinutes)
        )
    }
}
