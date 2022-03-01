package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.service.DomainRequestDetailsService
import generated.com.shykial.kScrapperCore.apis.DomainRequestDetailsApi
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DomainRequestDetailsController(
    private val domainRequestDetailsService: DomainRequestDetailsService,
) : DomainRequestDetailsApi {
    private val log = KotlinLogging.logger { }

    override suspend fun addDomainRequestDetails(
        domainRequestDetailsRequest: DomainRequestDetailsRequest
    ): ResponseEntity<Unit> {
        log.info("Registering domain request details request for domain: ${domainRequestDetailsRequest.domainName}")
        domainRequestDetailsService.addDomainRequestDetails(domainRequestDetailsRequest)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
