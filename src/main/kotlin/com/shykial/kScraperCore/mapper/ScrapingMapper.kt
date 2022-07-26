package com.shykial.kScraperCore.mapper

import com.shykial.kScraperCore.model.ScrapedData
import generated.com.shykial.kScraperCore.models.ScrapedDataResponse
import java.time.ZoneOffset

fun ScrapedData.toResponse() = ScrapedDataResponse(
    url = url,
    scrapedFields = scrapedFields,
    scrapingTimestamp = timestamp.atOffset(ZoneOffset.UTC)
)
