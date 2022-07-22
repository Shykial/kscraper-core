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
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant
import java.time.temporal.ChronoUnit

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
                    JWT.decode(it.token).run {
                        assertProperTokenGenerated(validUser)
                    }
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
            applicationUserRepository.findAll().toList().let {
                assertThat(it).isEmpty()
            }

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

    private fun ApplicationUser.assertProperUserPersisted(
        request: RegisterUserRequest
    ) {
        assertThat(login).isEqualTo(request.login)
        assertThat(email).isEqualTo(request.email)
        assertThat(passwordEncoder.matches(request.password, passwordHash)).isTrue
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
                    assertThat(it.errorType).isEqualTo(ErrorType.AUTHENTICATION_FAILURE)
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
                applicationUserRepository.findAll().toList().run { assertThat(this).isEmpty() }
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
                assertThat(applicationUserRepository.findByLogin(existingUser.login)).isEqualTo(existingUser)
            }
        }
    }

    private fun sampleValidUser(rawPassword: String) = ApplicationUser(
        login = "testLogin",
        passwordHash = passwordEncoder.encode(rawPassword),
        email = "testEmail@testDomain.com",
        role = UserRole.API_USER,
        isDisabled = false
    )

    private fun DecodedJWT.assertProperTokenGenerated(validUser: ApplicationUser) {
        assertThat(subject).isEqualTo(validUser.login)
        assertThat(claims[jwtProperties.rolesClaimName]?.asList(String::class.java)).isEqualTo(listOf(validUser.role.name))
        assertThat(issuer).isEqualTo(jwtProperties.issuer)
        assertThat(issuedAt.toInstant()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES))
        assertThat(expiresAt.toInstant()).isCloseTo(
            Instant.now().plusSeconds(60 * jwtProperties.validityInMinutes),
            within(1, ChronoUnit.MINUTES)
        )
    }
}
