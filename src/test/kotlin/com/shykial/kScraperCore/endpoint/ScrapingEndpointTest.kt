package com.shykial.kScraperCore.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.helper.Given
import com.shykial.kScraperCore.helper.KScraperRestTest
import com.shykial.kScraperCore.helper.RestTest.Companion.BASE_PATH
import com.shykial.kScraperCore.helper.RestTestWithAuthentication
import com.shykial.kScraperCore.helper.Then
import com.shykial.kScraperCore.helper.When
import com.shykial.kScraperCore.helper.extractingBody
import com.shykial.kScraperCore.helper.resource.SupportedDomain
import com.shykial.kScraperCore.helper.respond
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.helper.toMockServerHeaders
import com.shykial.kScraperCore.helper.toRequest
import com.shykial.kScraperCore.init.UsersInitializer
import com.shykial.kScraperCore.mocks.HttpCallMocker
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import com.shykial.kScraperCore.starter.MockServerStarter
import com.shykial.kScraperCore.starter.MockServerStarter.Companion.mockServerClient
import com.shykial.kScraperCore.starter.MockServerStarter.Companion.mockServerUrl
import com.shykial.kScraperCore.starter.RequiredServicesStarter
import generated.com.shykial.kScraperCore.models.ScrapedDataResponse
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

private const val SCRAPE_PATH = "$BASE_PATH/scrape"

@KScraperRestTest
class ScrapingEndpointTest(
    override val objectMapper: ObjectMapper,
    override val webTestClient: WebTestClient,
    override val usersInitializer: UsersInitializer,
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
    private val httpCallMocker: HttpCallMocker
) : RestTestWithAuthentication, RequiredServicesStarter, MockServerStarter {

    @BeforeEach
    fun setup() = runTest {
        launch { domainRequestDetailsRepository.deleteAll() }
        launch { extractingDetailsRepository.deleteAll() }
        mockServerClient.reset()
    }

    @AfterEach
    fun clearMock() {
        httpCallMocker.clearMock()
    }

    @Test
    fun `should respond with properly scraped response given proper configuration persisted`() = runTest {
        val domain = SupportedDomain.EURO_RTV_AGD
        val domainRequestDetails = DomainRequestDetails(
            domainName = domain.domainName,
            requestHeaders = domain.headers
        ).saveIn(domainRequestDetailsRepository)
        val extractingDetails = domain.extractingDetails(domainRequestDetails.id).saveIn(extractingDetailsRepository)
        val responseMapping = domain.responseMappings[0]
        val resourceUrl = "https://euro.com.pl/mocked-product.html"
        mockHttpResponse(resourceUrl = resourceUrl, headers = domain.headers, body = responseMapping.htmlContent)

        Given {
            apiUserAuthHeader()
            queryParam("url", resourceUrl)
            queryParam("fields", extractingDetails.fieldName)
        } When {
            get(SCRAPE_PATH)
        } Then {
            status(HttpStatus.OK)
            extractingBody<ScrapedDataResponse> {
                it.url shouldBe resourceUrl
                it.scrapedFields shouldBe mapOf(extractingDetails.fieldName to responseMapping.expectedFieldValue)
            }
        }
    }

    private fun mockHttpResponse(
        resourceUrl: String,
        headers: Map<String, String>,
        body: String
    ) {
        val resourceSubUrl = resourceUrl.substringAfter("//")
        httpCallMocker.mockHttpRequestCallUrl(
            originalUrl = resourceUrl,
            mockedUrl = "$mockServerUrl/$resourceSubUrl"
        )

        toRequest {
            withMethod("GET")
            withPath("/$resourceSubUrl")
            withHeaders(headers.toMockServerHeaders())
        } respond {
            withStatusCode(200)
            withBody(body)
        }
    }
}
