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
import generated.com.shykial.kScrapperCore.models.LoginRequest
import io.restassured.http.ContentType
import io.restassured.module.webtestclient.RestAssuredWebTestClient
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
                contentType(ContentType.JSON)
                body(loginRequest.toJsonString())
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
    }

    @Nested
    inner class NegativeOutcome {
        @Test
        fun `should throw exception when providing invalid credentials`() = runTest {
            val rawPassword = "testPassword2"
            val validUser = sampleValidUser(rawPassword).saveIn(applicationUserRepository)
            val invalidLoginRequest = LoginRequest(login = validUser.login, password = "invalidPassword")
            Given {
                contentType(ContentType.JSON)
                body(invalidLoginRequest.toJsonString())
            } When {
                post("/login")
            } Then {
                status(HttpStatus.UNAUTHORIZED)
                extractingBody<ErrorResponse> {
                    assertThat(it.errorType).isEqualTo(ErrorType.AUTHENTICATION_FAILURE)
                }
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
        assertThat(claims[jwtProperties.rolesClaimName]?.asList(String::class.java))
            .isEqualTo(listOf(validUser.role.name))
        assertThat(issuer).isEqualTo(jwtProperties.issuer)
        assertThat(issuedAt.toInstant()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES))
        assertThat(expiresAt.toInstant()).isCloseTo(
            Instant.now().plusSeconds(60 * jwtProperties.validityInMinutes),
            within(1, ChronoUnit.MINUTES)
        )
    }
}