package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.exception.NotFoundException
import com.shykial.kScrapperCore.helper.saveAllIn
import com.shykial.kScrapperCore.helper.saveIn
import com.shykial.kScrapperCore.mapper.toEntities
import com.shykial.kScrapperCore.mapper.updateWith
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsUpdateRequest
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
