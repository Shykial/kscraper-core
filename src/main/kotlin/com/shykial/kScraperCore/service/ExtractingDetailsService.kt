package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.exception.NotFoundException
import com.shykial.kScraperCore.helper.saveAllIn
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.mapper.toEntities
import com.shykial.kScraperCore.mapper.updateWith
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import generated.com.shykial.kScraperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScraperCore.models.ExtractingDetailsUpdateRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ExtractingDetailsService(
    private val extractingDetailsRepository: ExtractingDetailsRepository,
) {
    private val log = KotlinLogging.logger { }

    suspend fun addExtractingDetails(extractingDetailsRequest: ExtractingDetailsRequest) =
        extractingDetailsRequest
            .toEntities()
            .also { details ->
                log.info(
                    "Saving extractingDetails:${details.map { "DomainId: ${it.domainId}, fieldName: ${it.fieldName}]" }}"
                )
            }.saveAllIn(extractingDetailsRepository)

    suspend fun findByExtractingFieldDetailsId(extractingDetailsId: String) =
        extractingDetailsRepository.findById(extractingDetailsId)
            ?: throw NotFoundException("Extracting details not found for ID $extractingDetailsId")

    suspend fun findByDomainIdAndFieldNames(domainId: String, fieldNames: List<String>? = null) =
        if (fieldNames == null) extractingDetailsRepository.findByDomainId(domainId)
        else extractingDetailsRepository.findByDomainIdAndFieldNameIn(domainId, fieldNames)

    suspend fun updateExtractingDetails(id: String, extractingDetailsUpdateRequest: ExtractingDetailsUpdateRequest) {
        extractingDetailsRepository.findById(id)
            ?.updateWith(extractingDetailsUpdateRequest)
            ?.saveIn(extractingDetailsRepository)
            ?: throw NotFoundException("Extracting details not found for ID $id")
    }
}
