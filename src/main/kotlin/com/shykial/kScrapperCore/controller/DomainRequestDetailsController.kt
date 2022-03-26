package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.extensions.runSuspend
import com.shykial.kScrapperCore.helpers.toResponseEntity
import com.shykial.kScrapperCore.mapper.toResponse
import com.shykial.kScrapperCore.service.DomainRequestDetailsService
import generated.com.shykial.kScrapperCore.apis.DomainRequestDetailsApi
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DomainRequestDetailsController(
    private val domainRequestDetailsService: DomainRequestDetailsService,
) : DomainRequestDetailsApi {
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
        domainRequestDetailsRequest: DomainRequestDetailsRequest,
    ): ResponseEntity<DomainRequestDetailsResponse> = domainRequestDetailsRequest
        .also { log.info("Received addDomainRequestDetails request for domain: ${domainRequestDetailsRequest.domainName}") }
        .runSuspend(domainRequestDetailsService::addDomainRequestDetails)
        .toResponse()
        .toResponseEntity(HttpStatus.CREATED)
}
