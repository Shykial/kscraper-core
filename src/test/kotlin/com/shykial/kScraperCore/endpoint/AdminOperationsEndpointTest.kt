package com.shykial.kScraperCore.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.helper.Given
import com.shykial.kScraperCore.helper.KScraperRestTest
import com.shykial.kScraperCore.helper.RestTest.Companion.BASE_PATH
import com.shykial.kScraperCore.helper.RestTestWithAuthentication
import com.shykial.kScraperCore.helper.Then
import com.shykial.kScraperCore.helper.When
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.init.UsersInitializer
import com.shykial.kScraperCore.model.entity.ApplicationUser
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.repository.ApplicationUserRepository
import com.shykial.kScraperCore.starter.RequiredServicesStarter
import generated.com.shykial.kScraperCore.models.AccountState
import generated.com.shykial.kScraperCore.models.ChangeAccountStateRequest
import generated.com.shykial.kScraperCore.models.ChangeUserEmailRequest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils

private const val ADMINISTRATIVE_ENDPOINT_URL = "$BASE_PATH/administrative"
private const val CHANGE_ACCOUNT_STATE_ENDPOINT_URL = "$ADMINISTRATIVE_ENDPOINT_URL/change-account-state"
private const val CHANGE_ACCOUNT_EMAIL_ENDPOINT_URL = "$ADMINISTRATIVE_ENDPOINT_URL/change-email"

private const val INITIAL_USER_LOGIN = "initialUserLogin"

@KScraperRestTest
class AdminOperationsEndpointTest(
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    private val applicationUserRepository: ApplicationUserRepository,
    override val usersInitializer: UsersInitializer
) : RequiredServicesStarter, RestTestWithAuthentication {

    @BeforeEach
    fun setup() = runTest {
        applicationUserRepository.findByLogin(INITIAL_USER_LOGIN)
            ?.runSuspend(applicationUserRepository::delete)
    }

    @Nested
    inner class PositiveOutcome {
        @ParameterizedTest
        @MethodSource("changeAccountStateSource")
        fun `should properly change user's state on PUT request`(
            accountStateChangeMapping: AccountStateChangeMapping
        ) = runTest {
            val initialUser = createSampleInitialUser(enabled = accountStateChangeMapping.enabledFromStart)
                .saveIn(applicationUserRepository)
            val request = ChangeAccountStateRequest(
                accountLogin = initialUser.login,
                newState = accountStateChangeMapping.newState
            )

            Given {
                jsonBody(request)
            } When {
                put(CHANGE_ACCOUNT_STATE_ENDPOINT_URL)
            } Then {
                status(HttpStatus.NO_CONTENT)
                applicationUserRepository.findByLogin(initialUser.login)!!.run {
                    enabled shouldBe accountStateChangeMapping.newState.toExpectedEnabledBoolean()
                }
            }
        }

        @Test
        fun `should properly change user's email on PUT request`() = runTest {
            val initialEmail = "initialEmail@email.com"
            val newEmail = "newEmail@email.com"
            createSampleInitialUser(email = initialEmail).saveIn(applicationUserRepository)
            val request = ChangeUserEmailRequest(login = INITIAL_USER_LOGIN, newEmail = newEmail)

            Given {
                jsonBody(request)
            } When {
                put(CHANGE_ACCOUNT_EMAIL_ENDPOINT_URL)
            } Then {
                status(HttpStatus.NO_CONTENT)
                applicationUserRepository.findByLogin(INITIAL_USER_LOGIN)!!.run {
                    email shouldBe newEmail
                }
            }
        }

        private fun changeAccountStateSource() = listOf(
            AccountStateChangeMapping(
                enabledFromStart = true,
                newState = AccountState.ENABLED
            ),
            AccountStateChangeMapping(
                enabledFromStart = true,
                newState = AccountState.DISABLED
            ),
            AccountStateChangeMapping(
                enabledFromStart = false,
                newState = AccountState.ENABLED
            ),
            AccountStateChangeMapping(
                enabledFromStart = false,
                newState = AccountState.DISABLED
            )
        )
    }

    @Nested
    inner class NegativeOutcome {
        @Test
        fun `should return 403 FORBIDDEN when accessing endpoint without ADMIN access level`() = runTest {
            val request = ChangeAccountStateRequest(
                accountLogin = "sampleLogin",
                newState = AccountState.ENABLED
            )
            Given {
                devAuthHeader()
                jsonBody(request)
            } When {
                put(CHANGE_ACCOUNT_STATE_ENDPOINT_URL)
            } Then {
                status(HttpStatus.FORBIDDEN)
            }
        }

        @Test
        fun `should return NOT FOUND when changing account state for non-existing user`() = runTest {
            val request = ChangeAccountStateRequest(
                accountLogin = RandomStringUtils.randomAlphabetic(10),
                newState = AccountState.ENABLED
            )

            Given {
                jsonBody(request)
            } When {
                put(CHANGE_ACCOUNT_STATE_ENDPOINT_URL)
            } Then {
                status(HttpStatus.NOT_FOUND)
            }
        }

        @Test
        fun `should return NOT FOUND when changing email for non-existing user`() = runTest {
            val request = ChangeUserEmailRequest(
                login = RandomStringUtils.randomAlphabetic(10),
                newEmail = "newEmail@email.com"
            )

            Given {
                jsonBody(request)
            } When {
                put(CHANGE_ACCOUNT_EMAIL_ENDPOINT_URL)
            } Then {
                status(HttpStatus.NOT_FOUND)
            }
        }
    }

    data class AccountStateChangeMapping(
        val enabledFromStart: Boolean,
        val newState: AccountState
    )

    private fun AccountState.toExpectedEnabledBoolean() = when (this) {
        AccountState.ENABLED -> true
        AccountState.DISABLED -> false
    }

    private fun createSampleInitialUser(
        enabled: Boolean = true,
        email: String = "sampleEmail@email.com"
    ) = ApplicationUser(
        login = INITIAL_USER_LOGIN,
        passwordHash = "samplePasswordHash",
        email = email,
        role = UserRole.API_USER,
        enabled = enabled
    )
}
