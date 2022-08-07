package com.shykial.kScraperCore.controller

import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.helper.RestScope
import com.shykial.kScraperCore.helper.iterableFlow
import com.shykial.kScraperCore.mapper.toExtractingDetailsResponse
import com.shykial.kScraperCore.mapper.toResponse
import com.shykial.kScraperCore.service.ExtractingDetailsService
import generated.com.shykial.kScraperCore.apis.ExtractingDetailsApi
import generated.com.shykial.kScraperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScraperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScraperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScraperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScraperCore.models.ExtractingDetailsUpdateRequest
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ExtractingDetailsController(
    private val extractingDetailsService: ExtractingDetailsService
) : ExtractingDetailsApi, RestScope {
    private val log = KotlinLogging.logger { }

    override suspend fun findExtractingDetailsById(id: String): ResponseEntity<ExtractingDetailsResponse> {
        log.info("Received find extracting details request for extractingDetails ID: $id")
        return extractingDetailsService.findByExtractingFieldDetailsId(id)
            .toExtractingDetailsResponse()
            .toResponseEntity()
    }

    override fun findExtractingDetails(
        domainId: String,
        fieldNames: List<String>?
    ): ResponseEntity<Flow<ExtractingDetailsResponse>> = iterableFlow {
        log.info("Received find extracting details request for domainId: $domainId and fieldNames: $fieldNames")
        extractingDetailsService.findByDomainIdAndFieldNames(domainId, fieldNames)
            .map { it.toExtractingDetailsResponse() }
    }.toResponseEntity()

    override suspend fun addExtractingDetails(
        extractingDetailsRequest: ExtractingDetailsRequest
    ): ResponseEntity<AddExtractingDetailsResponse> = extractingDetailsRequest
        .also {
            log.info(
                "Received request for adding extracting details for domainId: ${it.domainId}" +
                    ", for fields: ${it.extractedFieldsDetails.map(ExtractedFieldDetails::fieldName)}"
            )
        }.runSuspend(extractingDetailsService::addExtractingDetails)
        .toResponse()
        .toResponseEntity(HttpStatus.CREATED)

    override suspend fun updateExtractingDetails(
        id: String,
        extractingDetailsUpdateRequest: ExtractingDetailsUpdateRequest
    ): ResponseEntity<Unit> {
        log.info("Received update extracting details request for extractingDetails ID: $id")
        extractingDetailsService.updateExtractingDetails(id, extractingDetailsUpdateRequest)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
