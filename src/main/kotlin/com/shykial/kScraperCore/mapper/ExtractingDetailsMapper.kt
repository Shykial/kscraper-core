package com.shykial.kScraperCore.mapper

import com.shykial.kScraperCore.exception.InvalidInputException
import com.shykial.kScraperCore.helper.decodeBase64
import com.shykial.kScraperCore.helper.toBase64String
import com.shykial.kScraperCore.model.entity.Attribute
import com.shykial.kScraperCore.model.entity.ExtractedProperty
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.model.entity.OwnText
import com.shykial.kScraperCore.model.entity.RegexReplacement
import com.shykial.kScraperCore.model.entity.Selector
import com.shykial.kScraperCore.model.entity.Text
import generated.com.shykial.kScraperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScraperCore.models.ExtractedPropertyType
import generated.com.shykial.kScraperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScraperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScraperCore.models.ExtractingDetailsUpdateRequest
import generated.com.shykial.kScraperCore.models.RegexReplacement as RegexReplacementInApi
import generated.com.shykial.kScraperCore.models.Selector as SelectorInApi

fun ExtractingDetailsRequest.toEntities() = extractedFieldsDetails.map {
    ExtractingDetails(
        domainId = domainId,
        fieldName = it.fieldName,
        selector = it.selector.toEntityModel(),
        extractedProperty = extractedPropertyFrom(it.extractedPropertyType, it.extractedPropertyValue),
        regexFilter = it.regexFilter?.decodeBase64()?.toRegex(),
        regexReplacements = it.regexReplacements?.map { rr -> rr.toEntityModel() }?.toMutableList()
    )
}

fun ExtractingDetails.toExtractingDetailsResponse(): ExtractingDetailsResponse {
    val (extractedPropertyType, extractedPropertyValue) = extractedProperty.toResponsePair()
    return ExtractingDetailsResponse(
        id = id,
        fieldName = fieldName,
        selector = SelectorInApi(value = selector.value, index = selector.index),
        extractedPropertyType = extractedPropertyType,
        extractedPropertyValue = extractedPropertyValue,
        regexFilter = regexFilter?.pattern?.toBase64String(),
        regexReplacements = regexReplacements?.map { it.toApiModel() }
    )
}

fun List<ExtractingDetails>.toResponse() = groupBy(ExtractingDetails::domainId)
    .entries.single().let { (domainId, extractingDetails) ->
        AddExtractingDetailsResponse(
            domainId = domainId,
            extractedFieldsDetails = extractingDetails.map { it.toExtractingDetailsResponse() }
        )
    }

fun ExtractingDetails.updateWith(request: ExtractingDetailsUpdateRequest) = apply {
    fieldName = request.fieldName
    selector = request.selector.toEntityModel()
    extractedProperty = extractedPropertyFrom(request.extractedPropertyType, request.extractedPropertyValue)
    regexReplacements = request.regexReplacements?.map { it.toEntityModel() }?.toMutableList()
}

private fun RegexReplacementInApi.toEntityModel() = RegexReplacement(
    regex = base64EncodedRegex.decodeBase64().toRegex(),
    replacement = replacement
)

private fun RegexReplacement.toApiModel() = RegexReplacementInApi(
    base64EncodedRegex = regex.pattern.toBase64String(),
    replacement = replacement
)

private fun ExtractedProperty.toResponsePair() =
    when (this) {
        is Attribute -> ExtractedPropertyType.ATTRIBUTE to attributeName
        is OwnText -> ExtractedPropertyType.OWN_TEXT to null
        is Text -> ExtractedPropertyType.TEXT to null
    }

private fun SelectorInApi.toEntityModel() = Selector(
    value = value,
    index = index
)

private fun extractedPropertyFrom(
    extractedPropertyType: ExtractedPropertyType,
    extractedPropertyValue: String?
) = when (extractedPropertyType) {
    ExtractedPropertyType.TEXT -> Text
    ExtractedPropertyType.OWN_TEXT -> OwnText
    ExtractedPropertyType.ATTRIBUTE -> Attribute(
        extractedPropertyValue
            ?: throw InvalidInputException("No value property value provided for Attribute property type")
    )
}
