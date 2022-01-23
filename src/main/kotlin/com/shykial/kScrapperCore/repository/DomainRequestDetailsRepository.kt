package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface DomainRequestDetailsRepository : ReactiveMongoRepository<DomainRequestDetails, String> {
    fun findByDomainName(domainName: String): Mono<DomainRequestDetails>
}
