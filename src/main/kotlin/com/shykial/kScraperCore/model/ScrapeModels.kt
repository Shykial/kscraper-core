package com.shykial.kScraperCore.model

import com.shykial.kScraperCore.model.entity.ExtractingDetails
import java.time.Instant

data class ScrapedData(
    val url: String,
    val scrapedFields: Map<ExtractingDetails, String>,
    val failedDetails: List<ExtractingDetails>,
    val timestamp: Instant = Instant.now()
)

data class ScrapingRequestMessage(
    val requestId: String,
    val scrapedResources: List<ScrapedResource>
)

data class ScrapedResource(
    val url: String,
    val fields: List<String>? = null
)

data class ScrapingResponseMessage(
    val requestId: String,
    val scrapingResults: List<ResourceScrapingResult>
)

data class ResourceScrapingResult(
    val url: String,
    val scrapingOutcome: ScrapingOutcome
)

sealed interface ScrapingOutcome {
    object Failure : ScrapingOutcome

    data class Success(
        val scrapedFields: Map<String, String>,
        val failedFields: List<String>,
        val timestamp: Instant
    ) : ScrapingOutcome
}
