package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.common.toResponseEntity
import com.shykial.kScrapperCore.extensions.runSuspend
import com.shykial.kScrapperCore.mapper.toResponse
import com.shykial.kScrapperCore.service.ExtractingDetailsService
import generated.com.shykial.kScrapperCore.apis.ExtractingDetailsApi
import generated.com.shykial.kScrapperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsResponse
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
        extractingDetailsRequest: ExtractingDetailsRequest,
    ): ResponseEntity<ExtractingDetailsResponse> = extractingDetailsRequest
        .also {
            log.info(
                "Received request for adding extracting details for domainId: ${extractingDetailsRequest.domainId}" +
                    ", for fields: ${extractingDetailsRequest.extractedFieldsDetails.map(ExtractedFieldDetails::fieldName)}"
            )
        }
        .runSuspend(extractingDetailsService::addExtractingDetails)
        .toResponse()
        .toResponseEntity(HttpStatus.CREATED)
}
