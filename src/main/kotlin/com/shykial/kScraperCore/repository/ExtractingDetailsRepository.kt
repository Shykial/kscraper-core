package com.shykial.kScraperCore.repository

import com.shykial.kScraperCore.model.entity.ExtractingDetails
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExtractingDetailsRepository : CoroutineCrudRepository<ExtractingDetails, String> {
    suspend fun findByDomainId(domainId: String): List<ExtractingDetails>

    suspend fun findByDomainIdAndFieldNameIn(domainId: String, fieldNames: List<String>): List<ExtractingDetails>
}
