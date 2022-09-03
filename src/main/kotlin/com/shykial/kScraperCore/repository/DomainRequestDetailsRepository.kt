package com.shykial.kScraperCore.repository

import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DomainRequestDetailsRepository : CoroutineCrudRepository<DomainRequestDetails, String> {
    suspend fun findByDomainName(domainName: String): DomainRequestDetails?

    suspend fun findByRequestTimeoutInMillis(timeoutInMillis: Int): DomainRequestDetails?

    @Aggregation(
        pipeline = [
            "{ \$match: { domainName: ?0 } }",
            "{ \$project: { domainRequestDetailsId: { \$toString: '\$_id' }, domainRequestDetails: '\$\$ROOT' } }",
            """
            { 
              ${'$'}lookup: {
                from: "extractingDetails",
                localField: "domainRequestDetailsId",
                foreignField: "domainId",
                as: "extractingDetails"
              }
            }
            """,
            "{ \$addFields:  { extractingDetails: { \$first: '\$extractingDetails' } } }"
        ]
    )
    suspend fun findAggregationMatch(domainName: String): AggregatedMatch?
}

data class AggregatedMatch(
    val domainRequestDetails: DomainRequestDetails,
    val extractingDetails: ExtractingDetails?
)
