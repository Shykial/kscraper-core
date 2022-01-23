package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.mapper.toEntity
import com.shykial.kScrapperCore.model.dto.DomainRequestDetailsRequest
import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import com.shykial.kScrapperCore.repository.DomainRequestDetailsRepository
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DomainRequestDetailsService(
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
) {
    private val log = KotlinLogging.logger { }

    suspend fun addDomainRequestDetails(domainRequestDetailsRequest: DomainRequestDetailsRequest): DomainRequestDetails =
        domainRequestDetailsRequest.also {
            log.info("Saving domainRequestDetails for domain ${it.domainName}")
        }.run { domainRequestDetailsRepository.save(toEntity()).awaitSingle() }
}
