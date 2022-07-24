package com.shykial.kScraperCore.staticImplementation

import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.entity.Attribute
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.model.entity.OwnText
import com.shykial.kScraperCore.model.entity.Text
import com.shykial.kScraperCore.useCase.ScrapeForDataUseCase
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import mu.KotlinLogging
import java.util.UUID

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
            domainRequestDetails.requestHeaders?.let { headers = it }
            domainRequestDetails.requestTimeoutInMillis?.let { timeout = it }
        }
        log.debug { "[$requestID] Sending request $preparedRequest" }
        response {
            log.debug {
                "[$requestID] Received response with status $responseStatus" +
                    " after ${System.currentTimeMillis() - startMillis} ms"
            }
            htmlDocument {
                ScrapedData(
                    url = resourceUrl,
                    scrapedFields = extractingDetails.associate { extractDetails(it) }
                )
            }
        }
    }

    private fun Doc.extractDetails(
        extractingDetails: ExtractingDetails
    ): Pair<String, String> = extractingDetails.let { details ->
        val element = details.selector.run { if (index == -1) findLast(value) else findByIndex(index, value) }
        val elementText = element.run {
            when (val extractedPropertyState = details.extractedProperty) {
                is Attribute -> attribute(extractedPropertyState.attributeName)
                is OwnText -> ownText
                is Text -> text
            }
        }
        details.fieldName to elementText.run {
            details.regexReplacements?.fold(this) { current, (regex, replacement) ->
                current.replace(regex, replacement)
            } ?: this
        }
    }
}
