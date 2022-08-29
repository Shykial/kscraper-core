package com.shykial.kScraperCore.controller

import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.helper.AllowedForDev
import com.shykial.kScraperCore.helper.RestScope
import com.shykial.kScraperCore.mapper.toResponse
import com.shykial.kScraperCore.service.DomainRequestDetailsService
import generated.com.shykial.kScraperCore.apis.DomainRequestDetailsApi
import generated.com.shykial.kScraperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScraperCore.models.DomainRequestDetailsResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
@AllowedForDev
class DomainRequestDetailsController(
    private val domainRequestDetailsService: DomainRequestDetailsService
) : DomainRequestDetailsApi, RestScope {
    private val log = KotlinLogging.logger { }

    override suspend fun findDomainRequestDetailsById(id: String): ResponseEntity<DomainRequestDetailsResponse> {
        log.info("Received getDomainRequestDetails request for domainRequestDetailsId: $id")
        return domainRequestDetailsService.findByDomainRequestDetailsId(id).toResponse().toResponseEntity()
    }

    override suspend fun findDomainRequestDetails(domainName: String): ResponseEntity<DomainRequestDetailsResponse> {
        log.info("Received getDomainRequestDetails request for domainName $domainName")
        return domainRequestDetailsService.findByDomainName(domainName).toResponse().toResponseEntity()
    }

    override suspend fun addDomainRequestDetails(
        domainRequestDetailsRequest: DomainRequestDetailsRequest
    ): ResponseEntity<DomainRequestDetailsResponse> = domainRequestDetailsRequest
        .also { log.info("Received addDomainRequestDetails request for domain: ${domainRequestDetailsRequest.domainName}") }
        .runSuspend(domainRequestDetailsService::addDomainRequestDetails)
        .toResponse()
        .toResponseEntity(HttpStatus.CREATED)

    override suspend fun updateDomainRequestDetails(
        id: String,
        domainRequestDetailsRequest: DomainRequestDetailsRequest
    ): ResponseEntity<Unit> {
        log.info("Received update domain request details request for domainRequestDetails ID: $id")
        domainRequestDetailsService.updateDomainRequestDetails(id, domainRequestDetailsRequest)
        return noContentResponseEntity()
    }
}
