package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.mapper.toAvroMessage
import com.shykial.kScraperCore.model.ScrapingRequestMessage
import com.shykial.kScraperCore.model.ScrapingResponseMessage
import com.shykial.kScraperCore.producer.ScrapingResponseMessagesProducer
import org.springframework.stereotype.Service

@Service
class ScrapingMessagesService(
    private val scrapingService: ScrapingService,
    private val scrapingResponseMessagesProducer: ScrapingResponseMessagesProducer
) {
    suspend fun handleScrapingRequestMessage(scrapingRequest: ScrapingRequestMessage) {
        val scrapingResults = scrapingService.scrapeResources(scrapingRequest.scrapedResources)

        val responseMessage = ScrapingResponseMessage(
            requestId = scrapingRequest.requestId,
            scrapingResults = scrapingResults
        )
        scrapingResponseMessagesProducer.publishScrapingResponseMessage(responseMessage.toAvroMessage())
    }
}
