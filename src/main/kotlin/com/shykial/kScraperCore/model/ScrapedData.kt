package com.shykial.kScraperCore.model

import java.time.Instant

data class ScrapedData(
    val url: String,
    val scrapedFields: Map<String, String>,
    val timestamp: Instant = Instant.now(),
)
