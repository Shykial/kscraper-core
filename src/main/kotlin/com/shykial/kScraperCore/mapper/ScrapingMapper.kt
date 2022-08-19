package com.shykial.kScraperCore.mapper

import com.shykial.kScraperCore.model.ResourceScrapingResult
import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.ScrapedResource
import com.shykial.kScraperCore.model.ScrapingOutcome
import com.shykial.kScraperCore.model.ScrapingRequestMessage
import com.shykial.kScraperCore.model.ScrapingResponseMessage
import generated.com.shykial.kScraperCore.avro.ScrapingFailure
import generated.com.shykial.kScraperCore.avro.ScrapingRequestMessageAvro
import generated.com.shykial.kScraperCore.avro.ScrapingResponseMessageAvro
import generated.com.shykial.kScraperCore.avro.ScrapingSuccess
import generated.com.shykial.kScraperCore.models.ScrapedDataResponse
import java.time.ZoneOffset

fun ScrapedData.toResponse() = ScrapedDataResponse(
    url = url,
    scrapedFields = scrapedFields.mapKeys { it.key.fieldName },
    failedFields = failedDetails.map { it.fieldName },
    scrapingTimestamp = timestamp.atOffset(ZoneOffset.UTC)
)

fun ScrapedData.toResourceScrapingResult() = ResourceScrapingResult(
    url = url,
    scrapingOutcome = ScrapingOutcome.Success(
        scrapedFields = scrapedFields.mapKeys { it.key.fieldName },
        failedFields = failedDetails.map { it.fieldName },
        timestamp = timestamp
    )
)

fun ScrapingResponseMessage.toAvroMessage() = ScrapingResponseMessageAvro(
    requestId,
    scrapingResults.map {
        generated.com.shykial.kScraperCore.avro.ResourceScrapingResult(
            it.url,
            it.scrapingOutcome.toAvroOutcome()
        )
    }
)

fun ScrapingRequestMessageAvro.toModel() = ScrapingRequestMessage(
    requestId = requestId,
    scrapedResources = scrapedResources.map { ScrapedResource(url = it.url, fields = it.fields) }
)

private fun ScrapingOutcome.toAvroOutcome() = when (this) {
    ScrapingOutcome.Failure -> ScrapingFailure.FAILURE
    is ScrapingOutcome.Success -> ScrapingSuccess(
        scrapedFields,
        failedFields,
        timestamp
    )
}
