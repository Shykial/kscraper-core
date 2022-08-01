package com.shykial.kScraperCore.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.helper.Given
import com.shykial.kScraperCore.helper.RestTest
import com.shykial.kScraperCore.helper.Then
import com.shykial.kScraperCore.helper.When
import com.shykial.kScraperCore.helper.resource.SupportedDomain
import com.shykial.kScraperCore.helper.respond
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.helper.toMockServerHeaders
import com.shykial.kScraperCore.helper.toRequest
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import com.shykial.kScraperCore.starter.MockServerStarter
import com.shykial.kScraperCore.starter.MongoDBStarter
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient

private const val SCRAPE_PATH = "/scrape"

@SpringBootTest
class ScrapingEndpointTest(
    override val objectMapper: ObjectMapper,
    override val webTestClient: WebTestClient,
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
    private val extractingDetailsRepository: ExtractingDetailsRepository
) : RestTest(), MongoDBStarter, MockServerStarter {

    @BeforeEach
    fun setup() = runTest {
        launch { domainRequestDetailsRepository.deleteAll() }
        launch { extractingDetailsRepository.deleteAll() }
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
        val mockedPath = "euro.com.pl"

        toRequest {
            withMethod("GET")
            withPath(mockedPath)
            withHeaders(domain.headers.toMockServerHeaders())
        } respond {
            withStatusCode(200)
            withBody(responseMapping.htmlContent)
        }

        Given {
            queryParam("url", "${MockServerStarter.mockServerUrl}/$mockedPath")
        } When {
            get(SCRAPE_PATH)
        } Then {
            status(HttpStatus.OK)
        }
//        println()
//        val result = skrape(AsyncFetcher) {
//            request {
//                url = "https://www.euro.com.pl/plyty-do-zabudowy/samsung-nz64h57479k.bhtml"
//                headers = mapOf(
//                    "authority" to "www.euro.com.pl",
//                    "accept" to "/*/",
//                    "accept-language" to "en-US,en;q=0.9",
//                    "connection" to "keep-alive"
//                )
//                timeout = 15_000
//            }
//            response {
//                this
//            }
//        }
//        println(result)
    }
}
