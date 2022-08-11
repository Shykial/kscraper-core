package com.shykial.kScraperCore.model

import com.shykial.kScraperCore.model.entity.ExtractingDetails
import java.time.Instant

data class ScrapedData(
    val url: String,
    val scrapedFields: Map<ExtractingDetails, String>,
    val failedDetails: List<ExtractingDetails>,
    val timestamp: Instant = Instant.now()
)
