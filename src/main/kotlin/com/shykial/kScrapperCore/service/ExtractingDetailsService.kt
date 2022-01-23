package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.mapper.toEntities
import com.shykial.kScrapperCore.model.dto.ExtractingDetailsRequest
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ExtractingDetailsService(private val extractingDetailsRepository: ExtractingDetailsRepository) {
    private val log = KotlinLogging.logger { }

    suspend fun addExtractingDetails(extractingDetailsRequest: ExtractingDetailsRequest): List<ExtractingDetails> =
        extractingDetailsRequest.toEntities()
            .also { details ->
                log.info("Saving extractingDetails:" +
                        details.map { "DomainId: ${it.domainId}, fieldName: ${it.fieldName}]" })
            }.run(extractingDetailsRepository::saveAll)
            .asFlow().toList()
}
