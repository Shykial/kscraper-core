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
    val scrapedData: List<ScrapedData>
)
