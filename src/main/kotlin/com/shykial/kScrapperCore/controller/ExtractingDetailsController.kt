package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.model.dto.ExtractedFieldDetails
import com.shykial.kScrapperCore.model.dto.ExtractingDetailsRequest
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.service.ExtractingDetailsService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("extracting-details")
class ExtractingDetailsController(
    private val extractingDetailsService: ExtractingDetailsService,
) {
    private val log = KotlinLogging.logger { }

    @PostMapping
    suspend fun addExtractingDetails(@RequestBody extractingDetailsRequest: ExtractingDetailsRequest):
            ResponseEntity<List<ExtractingDetails>> =
        extractingDetailsRequest.also {
            log.info(
                "Received request for adding extracting details for domainId: ${it.domainId}" +
                        ", for fields: ${it.extractedFieldDetails.map(ExtractedFieldDetails::fieldName)}"
            )
        }.run {
            val responseBody = extractingDetailsService.addExtractingDetails(this)
            ResponseEntity(responseBody, HttpStatus.CREATED)
        }
}

