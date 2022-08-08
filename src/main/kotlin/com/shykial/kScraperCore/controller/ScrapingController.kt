package com.shykial.kScraperCore.controller

import com.shykial.kScraperCore.helper.AllowedForApiUser
import com.shykial.kScraperCore.helper.RestScope
import com.shykial.kScraperCore.mapper.toResponse
import com.shykial.kScraperCore.service.ScrapingService
import generated.com.shykial.kScraperCore.apis.ScrapingApi
import generated.com.shykial.kScraperCore.models.ScrapedDataResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ScrapingController(
    private val scrapingService: ScrapingService
) : ScrapingApi, RestScope {
    private val log = KotlinLogging.logger { }

    @AllowedForApiUser
    override suspend fun scrapeResource(url: String, fields: List<String>?): ResponseEntity<ScrapedDataResponse> {
        log.info("Received request to scrape resource at url $url for fields $fields")
        return scrapingService.scrapeUrl(url, fields)
            .toResponse()
            .toResponseEntity()
    }
}
