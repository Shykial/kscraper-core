package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DomainRequestDetailsRepository : CoroutineCrudRepository<DomainRequestDetails, String> {
    suspend fun findByDomainName(domainName: String): DomainRequestDetails?

    suspend fun findByRequestTimeoutInMillis(timeoutInMillis: Int): DomainRequestDetails?
}
