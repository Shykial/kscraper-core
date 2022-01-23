package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ExtractingDetailsRepository : ReactiveMongoRepository<ExtractingDetails, String> {
    fun findByDomainId(domainId: String): Flux<ExtractingDetails>

    fun findByDomainIdAndFieldNameIn(domainId: String, fieldNames: List<String>): Flux<ExtractingDetails>
}
