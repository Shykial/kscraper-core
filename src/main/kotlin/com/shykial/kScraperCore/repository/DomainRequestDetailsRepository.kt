package com.shykial.kScraperCore.repository

import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DomainRequestDetailsRepository : CoroutineCrudRepository<DomainRequestDetails, String> {
    suspend fun findByDomainName(domainName: String): DomainRequestDetails?
}
