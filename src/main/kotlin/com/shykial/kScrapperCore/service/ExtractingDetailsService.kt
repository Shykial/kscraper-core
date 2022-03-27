package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.exception.NotFoundException
import com.shykial.kScrapperCore.mapper.toEntities
import com.shykial.kScrapperCore.mapper.updateWith
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsUpdateRequest
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ExtractingDetailsService(
    private val extractingDetailsRepository: ExtractingDetailsRepository,
) {
    private val log = KotlinLogging.logger { }

    suspend fun addExtractingDetails(extractingDetailsRequest: ExtractingDetailsRequest) =
        extractingDetailsRequest.toEntities().also { details ->
            log.info(
                "Saving extractingDetails:${details.map { "DomainId: ${it.domainId}, fieldName: ${it.fieldName}]" }}"
            )
        }.run(extractingDetailsRepository::saveAll).asFlow()

    suspend fun findByExtractingFieldDetailsId(extractingDetailsId: String) =
        extractingDetailsRepository.findById(extractingDetailsId).awaitSingleOrNull()
            ?: throw NotFoundException("Extracting details not found for ID $extractingDetailsId")

    fun findByDomainIdAndFieldNames(domainId: String, fieldNames: List<String>? = null) =
        if (fieldNames == null) extractingDetailsRepository.findByDomainId(domainId).asFlow()
        else extractingDetailsRepository.findByDomainIdAndFieldNameIn(domainId, fieldNames).asFlow()

    suspend fun updateExtractingDetails(id: String, extractingDetailsUpdateRequest: ExtractingDetailsUpdateRequest) {
        extractingDetailsRepository.findById(id).awaitSingleOrNull()
            ?.updateWith(extractingDetailsUpdateRequest)
            ?.run(extractingDetailsRepository::save)?.awaitSingle()
            ?: throw NotFoundException("Extracting details not found for ID $id")
    }
}
