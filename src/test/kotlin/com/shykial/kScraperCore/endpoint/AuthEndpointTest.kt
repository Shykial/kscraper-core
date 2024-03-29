package com.shykial.kScraperCore.endpoint

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.extension.plusMinutes
import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.helper.Given
import com.shykial.kScraperCore.helper.KScraperRestTest
import com.shykial.kScraperCore.helper.RestTest
import com.shykial.kScraperCore.helper.RestTest.Companion.BASE_PATH
import com.shykial.kScraperCore.helper.Then
import com.shykial.kScraperCore.helper.When
import com.shykial.kScraperCore.helper.extractingBody
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.helper.shouldBeWithin
import com.shykial.kScraperCore.model.entity.ApplicationUser
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.repository.ApplicationUserRepository
import com.shykial.kScraperCore.security.JwtProperties
import com.shykial.kScraperCore.starter.RequiredServicesStarter
import generated.com.shykial.kScraperCore.models.AuthToken
import generated.com.shykial.kScraperCore.models.ErrorResponse
import generated.com.shykial.kScraperCore.models.ErrorType
import generated.com.shykial.kScraperCore.models.IdResponse
import generated.com.shykial.kScraperCore.models.InvalidInputErrorResponse
import generated.com.shykial.kScraperCore.models.LoginRequest
import generated.com.shykial.kScraperCore.models.RegisterUserRequest
import generated.com.shykial.kScraperCore.models.RejectedField
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.Instant

private const val AUTH_ENDPOINT_URL = "$BASE_PATH/auth"

private const val SAMPLE_VALID_USER_LOGIN = "testLogin"

@KScraperRestTest
internal class AuthEndpointTest(
    override val objectMapper: ObjectMapper,
    override val webTestClient: WebTestClient,
    private val applicationUserRepository: ApplicationUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties
) : RestTest, RequiredServicesStarter {

    @BeforeEach
    fun setup() = runTest {
        applicationUserRepository.findByLogin(SAMPLE_VALID_USER_LOGIN)
            ?.runSuspend(applicationUserRepository::delete)
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
                post("$AUTH_ENDPOINT_URL/login")
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
            applicationUserRepository.findAll().toList().find { it.login == request.login }.shouldBeNull()

            Given {
                jsonBody(request)
            } When {
                post("$AUTH_ENDPOINT_URL/register")
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
                post("$AUTH_ENDPOINT_URL/login")
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
                password = "invalid_password"
            )

            Given {
                jsonBody(invalidRequest)
            } When {
                post("$AUTH_ENDPOINT_URL/register")
            } Then {
                status(HttpStatus.BAD_REQUEST)
                extractingBody<InvalidInputErrorResponse> {
                    it.errorType shouldBe ErrorType.INVALID_INPUT
                    it.rejectedFields shouldContainExactlyInAnyOrder listOf(
                        RejectedField(fieldName = "email", rejectedValue = invalidRequest.email),
                        RejectedField(fieldName = "password", rejectedValue = invalidRequest.password)
                    )
                }
                applicationUserRepository.findAll().toList().find { it.login == invalidRequest.login }.shouldBeNull()
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
                post("$AUTH_ENDPOINT_URL/register")
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
        login = SAMPLE_VALID_USER_LOGIN,
        passwordHash = passwordEncoder.encode(rawPassword),
        email = "testEmail@testDomain.com",
        role = UserRole.API_USER,
        enabled = true
    )

    private fun DecodedJWT.assertProperTokenGenerated(validUser: ApplicationUser) {
        subject shouldBe validUser.login
        claims[jwtProperties.rolesClaimName]?.asList(String::class.java) shouldBe listOf(validUser.role.name)
        issuer shouldBe jwtProperties.issuer
        issuedAtAsInstant.shouldBeWithin(
            margin = Duration.ofMinutes(1),
            other = Instant.now()
        )
        expiresAtAsInstant.shouldBeWithin(
            margin = Duration.ofMinutes(1),
            other = Instant.now().plusMinutes(jwtProperties.validityInMinutes)
        )
    }
}
