package com.shykial.kScraperCore.useCase

import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractingDetails

interface ScrapeForDataUseCase {
    suspend fun scrapeForData(
        resourceUrl: String,
        domainRequestDetails: DomainRequestDetails,
        extractingDetails: List<ExtractingDetails>
    ): ScrapedData
}
