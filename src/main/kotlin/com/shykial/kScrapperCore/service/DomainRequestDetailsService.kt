package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.exception.NotFoundException
import com.shykial.kScrapperCore.helper.saveIn
import com.shykial.kScrapperCore.mapper.toEntity
import com.shykial.kScrapperCore.mapper.updateWith
import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import com.shykial.kScrapperCore.repository.DomainRequestDetailsRepository
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DomainRequestDetailsService(
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
) {
    private val log = KotlinLogging.logger { }

    suspend fun findByDomainRequestDetailsId(domainRequestDetailsId: String): DomainRequestDetails =
        domainRequestDetailsRepository.findById(domainRequestDetailsId)
            ?: throw NotFoundException("Domain request details not found for ID $domainRequestDetailsId")

    suspend fun findByDomainName(domainName: String): DomainRequestDetails =
        domainRequestDetailsRepository.findByDomainName(domainName)
            ?: throw NotFoundException("Domain request details not found for domain $domainName")

    suspend fun addDomainRequestDetails(
        domainRequestDetailsRequest: DomainRequestDetailsRequest
    ): DomainRequestDetails = domainRequestDetailsRequest
        .toEntity()
        .also { log.info("Saving domainRequestDetails for domain ${it.domainName}") }
        .saveIn(domainRequestDetailsRepository)

    suspend fun updateDomainRequestDetails(id: String, domainRequestDetailsRequest: DomainRequestDetailsRequest) {
        domainRequestDetailsRepository.findById(id)
            ?.updateWith(domainRequestDetailsRequest)
            ?.saveIn(domainRequestDetailsRepository)
            ?: throw NotFoundException("Domain request details not found for ID $id")
    }
}
