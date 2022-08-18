package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.model.ScrapingRequestMessage
import com.shykial.kScraperCore.model.ScrapingResponseMessage
import com.shykial.kScraperCore.producer.ScrapingResponseMessagesProducer
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ScrapingMessagesService(
    private val scrapingService: ScrapingService,
    private val scrapingResponseMessagesProducer: ScrapingResponseMessagesProducer
) {
    private val log = KotlinLogging.logger { }

    suspend fun handleScrapingRequestMessage(scrapingRequest: ScrapingRequestMessage) {
        val scrapingResult = runCatching {
            scrapingService.scrapeResources(scrapingRequest.scrapedResources)
        }.getOrElse {
            log.error(it) {
                "Exception while handling scraping request message with requestID: ${scrapingRequest.requestId}"
            }
            emptyList()
        }
        val responseMessage = ScrapingResponseMessage(
            requestId = scrapingRequest.requestId,
            scrapedData = scrapingResult
        )
        scrapingResponseMessagesProducer.publishScrapingResponseMessage(responseMessage)
    }
}
