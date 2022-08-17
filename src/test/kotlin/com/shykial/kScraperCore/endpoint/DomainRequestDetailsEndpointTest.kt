package com.shykial.kScraperCore.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.helper.Given
import com.shykial.kScraperCore.helper.KScraperRestTest
import com.shykial.kScraperCore.helper.RestTest.Companion.BASE_PATH
import com.shykial.kScraperCore.helper.RestTestWithAuthentication
import com.shykial.kScraperCore.helper.Then
import com.shykial.kScraperCore.helper.When
import com.shykial.kScraperCore.helper.extractingBody
import com.shykial.kScraperCore.helper.findRefreshed
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.init.UsersInitializer
import com.shykial.kScraperCore.mapper.toEntity
import com.shykial.kScraperCore.mapper.toResponse
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScraperCore.starter.MongoDBStarter
import generated.com.shykial.kScraperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScraperCore.models.DomainRequestDetailsResponse
import generated.com.shykial.kScraperCore.models.ErrorResponse
import generated.com.shykial.kScraperCore.models.ErrorType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

private const val DOMAIN_REQUEST_DETAILS_ENDPOINT = "$BASE_PATH/domain-request-details"

@KScraperRestTest
internal class DomainRequestDetailsEndpointTest(
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    override val usersInitializer: UsersInitializer
) : RestTestWithAuthentication, MongoDBStarter {

    @BeforeEach
    fun setup() = runTest {
        domainRequestDetailsRepository.deleteAll()
    }

    @Nested
    inner class PositiveOutcome {

        @Test
        fun `should properly retrieve domain request details by domain name on GET request`() = runTest {
            val entity = getSampleDomainRequestDetails().saveIn(domainRequestDetailsRepository)

            Given {
                queryParam("domainName", entity.domainName)
            } When {
                get(DOMAIN_REQUEST_DETAILS_ENDPOINT)
            } Then {
                status(HttpStatus.OK)
                extractingBody<DomainRequestDetailsResponse> {
                    it shouldBe entity.toResponse()
                }
            }
        }

        @Test
        fun `should properly retrieve domain request details by ID on GET request`() = runTest {
            val entity = getSampleDomainRequestDetails().saveIn(domainRequestDetailsRepository)

            When {
                get("$DOMAIN_REQUEST_DETAILS_ENDPOINT/${entity.id}")
            } Then {
                status(HttpStatus.OK)
                extractingBody<DomainRequestDetailsResponse> {
                    it shouldBe entity.toResponse()
                }
            }
        }

        @Test
        fun `should add new domain request detail on POST request`() = runTest {
            val request = getSampleDomainRequestDetailsRequest()
            domainRequestDetailsRepository.findByDomainName(request.domainName).shouldBeNull()

            Given {
                jsonBody(request)
            } When {
                post(DOMAIN_REQUEST_DETAILS_ENDPOINT)
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<DomainRequestDetailsResponse> {
                    val entity = domainRequestDetailsRepository.findByDomainName(request.domainName)
                    it shouldBe entity?.toResponse()
                    it.domainName shouldBe request.domainName
                    it.requestHeaders shouldBe request.requestHeaders
                    it.requestTimeoutInMillis shouldBe request.requestTimeoutInMillis
                }
            }
        }

        @Test
        fun `should properly update domain request details on PUT request`() = runTest {
            val initialDomainRequestDetails = getSampleDomainRequestDetails()
                .saveIn(domainRequestDetailsRepository)
            val updateRequest = DomainRequestDetailsRequest(
                domainName = initialDomainRequestDetails.domainName,
                requestHeaders = initialDomainRequestDetails.requestHeaders?.plus("newHeader" to "newValue"),
                requestTimeoutInMillis = 5000
            )

            Given {
                jsonBody(updateRequest)
            } When {
                put("$DOMAIN_REQUEST_DETAILS_ENDPOINT/${initialDomainRequestDetails.id}")
            } Then {
                status(HttpStatus.NO_CONTENT)

                domainRequestDetailsRepository.findRefreshed(initialDomainRequestDetails).run {
                    domainName shouldBe updateRequest.domainName
                    requestHeaders shouldBe updateRequest.requestHeaders
                    requestTimeoutInMillis shouldBe updateRequest.requestTimeoutInMillis
                }
            }
        }
    }

    @Nested
    inner class NegativeOutcome {

        @Test
        fun `should return NOT FOUND error response when queried for non-existing domain request details domain name`() =
            runTest {
                Given {
                    queryParam("domainName", randomAlphanumeric(20))
                } When {
                    get(DOMAIN_REQUEST_DETAILS_ENDPOINT)
                } Then {
                    status(HttpStatus.NOT_FOUND)
                    extractingBody<ErrorResponse> {
                        it.errorType shouldBe ErrorType.NOT_FOUND
                    }
                }
            }

        @Test
        fun `should return CONFLICT error response when trying to add duplicate domain request details`() = runTest {
            val request = getSampleDomainRequestDetailsRequest()
            domainRequestDetailsRepository.save(request.toEntity())

            Given {
                jsonBody(request)
            } When {
                post(DOMAIN_REQUEST_DETAILS_ENDPOINT)
            } Then {
                status(HttpStatus.CONFLICT)
            }
        }

        @Test
        fun `should return 403 FORBIDDEN when trying to add domain request details with forbidden role`() = runTest {
            val request = getSampleDomainRequestDetailsRequest()

            Given {
                apiUserAuthHeader()
                jsonBody(request)
            } When {
                post(DOMAIN_REQUEST_DETAILS_ENDPOINT)
            } Then {
                status(HttpStatus.FORBIDDEN)
            }
        }
    }
}

private fun getSampleDomainRequestDetailsRequest() = DomainRequestDetailsRequest(
    domainName = "testDomain${randomAlphabetic(5)}1.com",
    requestHeaders = mapOf(
        "firstHeader" to "firstHeaderValue",
        "secondHeader" to "secondHeaderValue"
    ),
    requestTimeoutInMillis = 1500
)

private fun getSampleDomainRequestDetails() = DomainRequestDetails(
    domainName = "testDomain${randomAlphabetic(5)}2.com",
    requestHeaders = mapOf(
        "firstHeader2" to "firstHeaderValue2",
        "secondHeader2" to "secondHeaderValue2"
    ),
    requestTimeoutInMillis = 1500
)
