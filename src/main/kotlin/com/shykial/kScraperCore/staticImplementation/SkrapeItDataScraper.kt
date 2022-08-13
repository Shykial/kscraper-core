package com.shykial.kScraperCore.staticImplementation

import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractedProperty
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.useCase.ScrapeForDataUseCase
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.Request
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import mu.KotlinLogging
import java.util.UUID

private const val USER_AGENT_HEADER_NAME = "user-agent"

object SkrapeItDataScraper : ScrapeForDataUseCase {
    private val log = KotlinLogging.logger { }

    override suspend fun scrapeForData(
        resourceUrl: String,
        domainRequestDetails: DomainRequestDetails,
        extractingDetails: List<ExtractingDetails>
    ): ScrapedData = skrape(AsyncFetcher) {
        val requestID = UUID.randomUUID()
        val startMillis = System.currentTimeMillis()
        request {
            url = resourceUrl
            domainRequestDetails.requestHeaders?.let { fillHeaders(it) }
            domainRequestDetails.requestTimeoutInMillis?.let { timeout = it }
        }
        log.debug { "[$requestID] Sending request $preparedRequest" }
        response {
            log.debug {
                "[$requestID] Received response with status $responseStatus" +
                    " after ${System.currentTimeMillis() - startMillis} ms"
            }
            htmlDocument {
                processHtmlDocument(extractingDetails, resourceUrl)
            }
        }
    }

    private fun Doc.processHtmlDocument(
        extractingDetails: List<ExtractingDetails>,
        resourceUrl: String
    ): ScrapedData {
        val properlyExtracted = mutableMapOf<ExtractingDetails, String>()
        val failedFields = mutableListOf<ExtractingDetails>()
        extractingDetails
            .associateWith { extractDetails(it) }
            .forEach { (details, scrapedValue) ->
                if (scrapedValue != null) properlyExtracted[details] = scrapedValue
                else failedFields.add(details)
            }
        return ScrapedData(
            url = resourceUrl,
            scrapedFields = properlyExtracted,
            failedDetails = failedFields
        ).also { log.info("finished scraping with result: ${it.scrapedFields}") }
    }

    private fun Request.fillHeaders(customHeaders: Map<String, String>) {
        customHeaders.entries
            .find { (key, _) -> key.equals(USER_AGENT_HEADER_NAME, ignoreCase = true) }
            ?.let { (key, value) ->
                userAgent = value
                headers = customHeaders - key
            } ?: run { headers = customHeaders }
    }

    private fun Doc.extractDetails(
        extractingDetails: ExtractingDetails
    ): String? = runCatching {
        val element = extractingDetails.selector.run { if (index == -1) findLast(value) else findByIndex(index, value) }
        val rawText = element.run {
            when (val extractedProperty = extractingDetails.extractedProperty) {
                is ExtractedProperty.Attribute -> attribute(extractedProperty.attributeName)
                is ExtractedProperty.OwnText -> ownText
                is ExtractedProperty.Text -> text
                is ExtractedProperty.Html -> html
            }
        }
        val filteredText = extractingDetails.regexFilter?.find(rawText)?.value ?: rawText
        extractingDetails.regexReplacements
            ?.fold(filteredText) { current, (regex, replacement) -> current.replace(regex, replacement) }
            ?: filteredText
    }.getOrNull()
}
