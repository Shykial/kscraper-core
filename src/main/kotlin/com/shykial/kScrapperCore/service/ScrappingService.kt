package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.model.entity.Attribute
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.model.entity.OwnText
import com.shykial.kScrapperCore.model.entity.Text
import com.shykial.kScrapperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

private val domainPartRegex = Regex("(?<=//).*?(?=/)")

@Service
class ScrappingService(
    private val DomainRequestDetailsRepository: DomainRequestDetailsRepository,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
) {
    private val log = KotlinLogging.logger { }

    suspend fun scrapeUrl(productUrl: String, scrapedFields: List<String>? = null) = coroutineScope {
        log.info("Scraping url $productUrl for fields $scrapedFields")
        val domainDetails = DomainRequestDetailsRepository.findByDomainName(productUrl.domainPart()).awaitSingle()
        val extractingDetails = (
            scrapedFields?.let {
                extractingDetailsRepository.findByDomainIdAndFieldNameIn(domainDetails.id, it)
            } ?: extractingDetailsRepository.findByDomainId(domainDetails.id)
            ).asFlow().toList()
        skrape(AsyncFetcher) {
            request {
                url = productUrl
                domainDetails.requestHeaders?.let { headers = it }
                domainDetails.requestTimeoutInMillis?.let { timeout = it }
            }
            response {
                htmlDocument {
                    ScrappedData(
                        url = productUrl,
                        scrappedFields = extractingDetails.associate(::extractDetails)
                    )
                }
            }
        }
    }
}

private fun Doc.extractDetails(extractingDetails: ExtractingDetails): Pair<String, String> = with(extractingDetails) {
    val element = selector.run { if (index == -1) findLast(value) else findByIndex(index, value) }
    val elementText = element.run {
        when (extractedProperty) {
            is Attribute -> attribute(extractedProperty.attributeName)
            OwnText -> ownText
            Text -> text
        }
    }
    fieldName to elementText.run {
        regexReplacements?.fold(this) { current, (regex, replacement) ->
            current.replace(regex, replacement)
        } ?: this
    }
}

data class ScrappedData(
    val url: String,
    val scrappedFields: Map<String, String>,
    val timestamp: Instant = Instant.now(),
)

private fun String.domainPart() = domainPartRegex.find(this)?.value
    ?: throw IllegalArgumentException("cannot obtain domain from url")
