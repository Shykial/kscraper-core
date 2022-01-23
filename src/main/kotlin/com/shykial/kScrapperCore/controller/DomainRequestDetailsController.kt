package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.model.dto.DomainRequestDetailsRequest
import com.shykial.kScrapperCore.service.DomainRequestDetailsService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("domain-request-details")
class DomainRequestDetailsController(
    private val domainRequestDetailsService: DomainRequestDetailsService,
) {
    private val log = KotlinLogging.logger { }

    @PostMapping
    suspend fun addDomainRequestDetails(@RequestBody domainRequestDetailsRequest: DomainRequestDetailsRequest) =
        domainRequestDetailsRequest.also {
            log.info("Registering domain request details request for domain: ${it.domainName}")
        }.run {
            val responseBody = domainRequestDetailsService.addDomainRequestDetails(this)
            ResponseEntity(responseBody, HttpStatus.CREATED)
        }
}