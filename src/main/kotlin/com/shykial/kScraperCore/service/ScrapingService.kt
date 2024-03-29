package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.exception.NotFoundException
import com.shykial.kScraperCore.mapper.toResourceScrapingResult
import com.shykial.kScraperCore.model.ResourceScrapingResult
import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.ScrapedResource
import com.shykial.kScraperCore.model.ScrapingOutcome
import com.shykial.kScraperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import com.shykial.kScraperCore.useCase.ScrapeForDataUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val DOMAIN_PART_FILTER_REGEX = Regex("""http(s)?://|www\.|/.*""")

@Service
class ScrapingService(
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
    private val scrapeForDataUseCase: ScrapeForDataUseCase,
    private val scrapingFailureDetectionService: ScrapingFailureDetectionService
) {
    private val log = KotlinLogging.logger { }

    suspend fun scrapeResources(resources: List<ScrapedResource>): List<ResourceScrapingResult> = coroutineScope {
        resources
            .map { resource ->
                async {
                    runCatching { scrapeUrl(resource.url, resource.fields) }
                        .onFailure { log.error(it) { "Exception while handling scraping resource: $resource" } }
                        .getOrNull()
                        ?.toResourceScrapingResult()
                        ?: ResourceScrapingResult(url = resource.url, scrapingOutcome = ScrapingOutcome.Failure)
                }
            }.awaitAll()
    }

    suspend fun scrapeUrl(
        resourceUrl: String,
        scrapedFields: List<String>? = null
    ): ScrapedData {
        log.info("Scraping url $resourceUrl for fields $scrapedFields")
        val domainName = resourceUrl.readDomainName()
        val domainDetails = domainRequestDetailsRepository.findByDomainName(domainName)
            ?: throw NotFoundException("Domain request details for domain name $domainName not found")
        val extractingDetails = scrapedFields?.let {
            extractingDetailsRepository.findByDomainIdAndFieldNameIn(domainDetails.id, it)
        } ?: extractingDetailsRepository.findByDomainId(domainDetails.id)

        if (extractingDetails.isEmpty()) throw NotFoundException(
            buildString {
                append("No extracting details found. Searching for details by: ")
                append("Domain ID: ${domainDetails.id}")
                scrapedFields?.let { append(", scraped fields: $it") }
            }
        )
        return scrapingFailureDetectionService.runDetectingScrapingFailures(domainDetails) { attempt ->
            log.info("Scraping for data for resource $resourceUrl, attempt $attempt")
            scrapeForDataUseCase.scrapeForData(
                resourceUrl = resourceUrl,
                domainRequestDetails = domainDetails,
                extractingDetails = extractingDetails
            )
        }
    }
}

private fun String.readDomainName() = replace(DOMAIN_PART_FILTER_REGEX, "")
