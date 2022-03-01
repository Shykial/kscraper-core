package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.service.ExtractingDetailsService
import generated.com.shykial.kScrapperCore.apis.ExtractingDetailsApi
import generated.com.shykial.kScrapperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ExtractingDetailsController(
    private val extractingDetailsService: ExtractingDetailsService,
) : ExtractingDetailsApi {
    private val log = KotlinLogging.logger { }

    override suspend fun addExtractingDetails(
        extractingDetailsRequest: ExtractingDetailsRequest
    ): ResponseEntity<Unit> {
        log.info(
            "Received request for adding extracting details for domainId: ${extractingDetailsRequest.domainId}" +
                ", for fields: ${extractingDetailsRequest.extractedFieldsDetails.map(ExtractedFieldDetails::fieldName)}"
        )
        extractingDetailsService.addExtractingDetails(extractingDetailsRequest)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
