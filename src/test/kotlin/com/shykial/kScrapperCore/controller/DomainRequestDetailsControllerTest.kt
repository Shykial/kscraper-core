package com.shykial.kScrapperCore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScrapperCore.helpers.Given
import com.shykial.kScrapperCore.helpers.RestTest
import com.shykial.kScrapperCore.helpers.Then
import com.shykial.kScrapperCore.helpers.When
import com.shykial.kScrapperCore.helpers.awaitAndAssertNull
import com.shykial.kScrapperCore.helpers.extractingBody
import com.shykial.kScrapperCore.mapper.toEntity
import com.shykial.kScrapperCore.mapper.toResponse
import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import com.shykial.kScrapperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScrapperCore.starter.MongoDBStarter
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsResponse
import generated.com.shykial.kScrapperCore.models.ErrorResponse
import io.restassured.http.ContentType
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

@SpringBootTest
internal class DomainRequestDetailsControllerTest(
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
) : MongoDBStarter, RestTest() {

    @BeforeEach
    fun setup() {
        domainRequestDetailsRepository.deleteAll().block()
    }

    @Nested
    inner class PositiveOutcome {

        @Test
        fun `should find domain request details by domain name`() = runTest {
            val entity = sampleDomainRequestDetails
            domainRequestDetailsRepository.save(entity).awaitSingle()

            Given {
                queryParam("domainName", entity.domainName)
            } When {
                get("/domain-request-details")
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
                post("/domain-request-details")
            } Then {
                status(HttpStatus.CREATED)
            }

            domainRequestDetailsRepository.findByDomainName(request.domainName).awaitSingle()
                .let { assertThat(it).isEqualTo(request.toEntity()) }
        }
    }

    @Nested
    inner class NegativeOutcome {

        @Test
        fun `should return NOT FOUND error response when queried for non-existing domain request details domain name`() {
            Given {
                queryParam("domainName", randomAlphanumeric(20))
            } When {
                get("/domain-request-details")
            } Then {
                status(HttpStatus.NOT_FOUND)
                extractingBody<ErrorResponse> {
                    assertThat(it.errorType).isEqualTo(ErrorResponse.ErrorType.NOT_FOUND)
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
                post("/domain-request-details")
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

private
val sampleDomainRequestDetails = DomainRequestDetails(
    domainName = "testDomain2.com",
    requestHeaders = mapOf(
        "firstHeader2" to "firstHeaderValue2",
        "secondHeader2" to "secondHeaderValue2"
    ),
    requestTimeoutInMillis = 1500
)
