package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.model.entity.Attribute
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.model.entity.OwnText
import com.shykial.kScrapperCore.model.entity.Text
import com.shykial.kScrapperCore.repository.ScrapeRecipeRepository
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import java.time.Instant

private val domainPartRegex = Regex("(?<=//).*?(?=/)")

class ScrappingService(private val scrapeRecipeRepository: ScrapeRecipeRepository) {
    private val log = KotlinLogging.logger { }

    suspend fun scrapeUrl(productUrl: String, scrapingFields: List<String>? = null) = coroutineScope {
        log.info("Scraping url $productUrl for fields $scrapingFields")
        scrapeRecipeRepository.findByDomainName(productUrl.domainPart()).awaitSingle().run {
            skrape(AsyncFetcher) {
                request {
                    url = productUrl
                    requestHeaders?.let { headers = it }
                    requestTimeout?.let { timeout = it }
                }
                response {
                    htmlDocument {
                        val scrappedFields = scrapingFields?.associateWith { field ->
                            extractingDetails[field]?.let {
                                extractDetails(it)
                            } ?: "Scraping recipe not present for field $field"
                        } ?: extractingDetails.mapValues { extractDetails(it.value) }

                        ScrappedData(
                            url = productUrl,
                            scrappedFields = scrappedFields
                        )
                    }
                }
            }
        }
    }
}

private fun Doc.extractDetails(extractingDetails: ExtractingDetails) = with(extractingDetails) {
    selector.run {
        if (index == -1) findLast(value) else findByIndex(index, value)
    }.run {
        when (extractedProperty) {
            is Attribute -> attribute(extractedProperty.attributeName)
            OwnText -> ownText
            Text -> text
        }
    }.run {
        regexReplacements.fold(this) { current, replacement ->
            current.replace(replacement.regex, replacement.replacement)
        }
    }
}

data class ScrappedData(
    val url: String,
    val scrappedFields: Map<String, String>,
    val timestamp: Instant = Instant.now(),
)

private fun String.domainPart() = domainPartRegex.find(this)?.value
    ?: throw IllegalArgumentException("cannot obtain domain from url")
