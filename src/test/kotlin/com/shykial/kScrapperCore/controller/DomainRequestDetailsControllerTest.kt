package com.shykial.kScrapperCore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScrapperCore.helper.Given
import com.shykial.kScrapperCore.helper.RestTest
import com.shykial.kScrapperCore.helper.Then
import com.shykial.kScrapperCore.helper.When
import com.shykial.kScrapperCore.helper.assertFieldsToBeEqual
import com.shykial.kScrapperCore.helper.awaitAndAssertNull
import com.shykial.kScrapperCore.helper.extractingBody
import com.shykial.kScrapperCore.mapper.toEntity
import com.shykial.kScrapperCore.mapper.toResponse
import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import com.shykial.kScrapperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScrapperCore.starter.MongoDBStarter
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsResponse
import generated.com.shykial.kScrapperCore.models.ErrorResponse
import generated.com.shykial.kScrapperCore.models.ErrorResponse.ErrorType
import io.restassured.http.ContentType
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

private const val DOMAIN_REQUEST_DETAILS_ENDPOINT = "/domain-request-details"

@SpringBootTest
internal class DomainRequestDetailsControllerTest(
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
) : RestTest(), MongoDBStarter {

    init {
        RestAssuredWebTestClient.basePath = DOMAIN_REQUEST_DETAILS_ENDPOINT
    }

    @BeforeEach
    fun setup() {
        domainRequestDetailsRepository.deleteAll().block()
    }

    @Nested
    inner class PositiveOutcome {

        @Test
        fun `should properly retrieve domain request details by domain name on GET request`() = runTest {
            val entity = sampleDomainRequestDetails
            domainRequestDetailsRepository.save(entity).awaitSingle()

            Given {
                queryParam("domainName", entity.domainName)
            } When {
                get()
            } Then {
                status(HttpStatus.OK)
                extractingBody<DomainRequestDetailsResponse> {
                    assertThat(it).isEqualTo(entity.toResponse())
                }
            }
        }

        @Test
        fun `should add new domain request detail on POST request`() = runTest {
            val request = sampleDomainRequestDetailsRequest
            domainRequestDetailsRepository.findByDomainName(request.domainName).awaitAndAssertNull()

            Given {
                contentType(ContentType.JSON)
                body(request.toJsonString())
            } When {
                post()
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<DomainRequestDetailsResponse> {
                    val entity = domainRequestDetailsRepository.findByDomainName(request.domainName).awaitSingle()
                    assertThat(it).isEqualTo(entity.toResponse())
                }
            }
        }

        @Test
        fun `should properly update domain request details on PUT request`() = runTest {
            val initialDomainRequestDetails = sampleDomainRequestDetails
                .run(domainRequestDetailsRepository::save).awaitSingle()
            val updateRequest = DomainRequestDetailsRequest(
                domainName = initialDomainRequestDetails.domainName,
                requestHeaders = initialDomainRequestDetails.requestHeaders?.plus("newHeader" to "newValue"),
                requestTimeoutInMillis = 5000
            )

            Given {
                contentType(ContentType.JSON)
                body(updateRequest.toJsonString())
            } When {
                put("/${initialDomainRequestDetails.id}")
            } Then {
                status(HttpStatus.NO_CONTENT)

                domainRequestDetailsRepository.findById(initialDomainRequestDetails.id).awaitSingle().run {
                    assertFieldsToBeEqual(
                        domainName to updateRequest.domainName,
                        requestHeaders to updateRequest.requestHeaders,
                        requestTimeoutInMillis to updateRequest.requestTimeoutInMillis
                    )
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
                    get()
                } Then {
                    status(HttpStatus.NOT_FOUND)
                    extractingBody<ErrorResponse> {
                        assertThat(it.errorType).isEqualTo(ErrorType.NOT_FOUND)
                    }
                }
            }

        @Test
        fun `should return CONFLICT error response when trying to add duplicate domain request details`() = runTest {
            val request = sampleDomainRequestDetailsRequest
            domainRequestDetailsRepository.save(request.toEntity()).awaitSingle()

            Given {
                contentType(ContentType.JSON)
                body(request.toJsonString())
            } When {
                post()
            } Then {
                status(HttpStatus.CONFLICT)
            }
        }
    }
}

private val sampleDomainRequestDetailsRequest = DomainRequestDetailsRequest(
    domainName = "testDomain1.com",
    requestHeaders = mapOf(
        "firstHeader" to "firstHeaderValue",
        "secondHeader" to "secondHeaderValue"
    ),
    requestTimeoutInMillis = 1500
)

private val sampleDomainRequestDetails = DomainRequestDetails(
    domainName = "testDomain2.com",
    requestHeaders = mapOf(
        "firstHeader2" to "firstHeaderValue2",
        "secondHeader2" to "secondHeaderValue2"
    ),
    requestTimeoutInMillis = 1500
)
