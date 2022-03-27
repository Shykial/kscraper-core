package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.extension.runSuspend
import com.shykial.kScrapperCore.helper.toResponseEntity
import com.shykial.kScrapperCore.mapper.toExtractingDetailsResponse
import com.shykial.kScrapperCore.mapper.toResponse
import com.shykial.kScrapperCore.service.ExtractingDetailsService
import generated.com.shykial.kScrapperCore.apis.ExtractingDetailsApi
import generated.com.shykial.kScrapperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsUpdateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ExtractingDetailsController(
    private val extractingDetailsService: ExtractingDetailsService,
) : ExtractingDetailsApi {
    private val log = KotlinLogging.logger { }

    override suspend fun findExtractingDetailsById(id: String): ResponseEntity<ExtractingDetailsResponse> {
        log.info("Received find extracting details request for extractingDetails ID: $id")
        return extractingDetailsService.findByExtractingFieldDetailsId(id)
            .toExtractingDetailsResponse()
            .toResponseEntity()
    }

    override suspend fun updateExtractingDetails(
        id: String,
        extractingDetailsUpdateRequest: ExtractingDetailsUpdateRequest,
    ): ResponseEntity<Unit> {
        log.info("Received update extracting details request for extractingDetails ID: $id")
        extractingDetailsService.updateExtractingDetails(id, extractingDetailsUpdateRequest)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    override fun findExtractingDetails(
        domainId: String,
        fieldNames: List<String>?,
    ): ResponseEntity<Flow<ExtractingDetailsResponse>> {
        log.info("Received find extracting details request for domainId: $domainId and fieldNames: $fieldNames")
        return extractingDetailsService.findByDomainIdAndFieldNames(domainId, fieldNames)
            .map { it.toExtractingDetailsResponse() }
            .toResponseEntity()
    }

    override suspend fun addExtractingDetails(
        extractingDetailsRequest: ExtractingDetailsRequest,
    ): ResponseEntity<AddExtractingDetailsResponse> = extractingDetailsRequest
        .also {
            log.info(
                "Received request for adding extracting details for domainId: ${it.domainId}" +
                        ", for fields: ${it.extractedFieldsDetails.map(ExtractedFieldDetails::fieldName)}"
            )
        }.runSuspend(extractingDetailsService::addExtractingDetails)
        .toResponse()
        .toResponseEntity(HttpStatus.CREATED)
}
