package com.shykial.kScraperCore.staticImplementation

import com.shykial.kScraperCore.helper.resource.ResponseMapping
import com.shykial.kScraperCore.helper.resource.SupportedDomain
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import io.kotest.matchers.maps.shouldContain
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import it.skrape.fetcher.Request
import it.skrape.fetcher.Result
import it.skrape.fetcher.Scraper
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SkrapeItDataScraperTest {
    private val domainRequestDetailsMock = mockk<DomainRequestDetails>(relaxed = true)

    @BeforeAll
    fun setUp() {
        mockkConstructor(Scraper::class)
    }

    @AfterAll
    fun tearDown() {
        unmockkConstructor(Scraper::class)
    }

    @ParameterizedTest
    @MethodSource("responseMappingsSource")
    fun `should properly retrieve scraped details from HTTP response body`(
        extractingDetails: ExtractingDetails,
        responseMapping: ResponseMapping
    ) = runTest {
        coEvery { anyConstructed<Scraper<Request>>().scrape() } returns resultWithBody(responseMapping.htmlContent)
        val scrapedData = SkrapeItDataScraper.scrapeForData(
            resourceUrl = "",
            domainRequestDetails = domainRequestDetailsMock,
            extractingDetails = listOf(extractingDetails)
        )
        scrapedData.scrapedFields shouldContain (extractingDetails to responseMapping.expectedFieldValue)
    }

    private fun responseMappingsSource() = enumValues<SupportedDomain>()
        .flatMap { domain ->
            val extractingDetails = domain.extractingDetails(domainRequestDetailsMock.id)
            domain.responseMappings.map {
                Arguments.of(
                    Named.of("for domain ${domain.name}", extractingDetails),
                    Named.of("with expected value ${it.expectedFieldValue}", it)
                )
            }
        }

    private fun resultWithBody(responseBody: String) = Result(
        responseBody = responseBody,
        responseStatus = Result.Status(200, ""),
        contentType = "text/html; charset=UTF-8",
        headers = mapOf(),
        baseUri = "mockedBaseUri",
        cookies = listOf()
    )
}
