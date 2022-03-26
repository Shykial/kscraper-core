package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.exception.InvalidInputException
import com.shykial.kScrapperCore.helpers.decodeBase64
import com.shykial.kScrapperCore.helpers.toBase64String
import com.shykial.kScrapperCore.model.entity.Attribute
import com.shykial.kScrapperCore.model.entity.ExtractedProperty
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.model.entity.OwnText
import com.shykial.kScrapperCore.model.entity.RegexReplacement
import com.shykial.kScrapperCore.model.entity.Selector
import com.shykial.kScrapperCore.model.entity.Text
import generated.com.shykial.kScrapperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.ExtractedPropertyType
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import generated.com.shykial.kScrapperCore.models.Selector as SelectorInResponse

fun ExtractingDetailsRequest.toEntities() = extractedFieldsDetails.map {
    ExtractingDetails(
        domainId = domainId,
        fieldName = it.fieldName,
        selector = it.selector.toEntityModel(),
        extractedProperty = extractedPropertyFrom(it.extractedPropertyType, it.extractedPropertyValue),
        regexReplacements = it.base64EncodedRegexReplacements?.map { (key, value) ->
            RegexReplacement(
                regex = Regex(decodeBase64(key)),
                replacement = value
            )
        }
    )
}

fun ExtractingDetails.toExtractingDetailsResponse(): ExtractingDetailsResponse {
    val (extractedPropertyType, extractedPropertyValue) = extractedProperty.toResponsePair()
    return ExtractingDetailsResponse(
        id = id,
        fieldName = fieldName,
        selector = SelectorInResponse(value = selector.value, index = selector.index),
        extractedPropertyType = extractedPropertyType,
        extractedPropertyValue = extractedPropertyValue,
        base64EncodedRegexReplacements = regexReplacements?.associate {
            it.regex.pattern.toBase64String() to it.replacement
        }
    )
}

suspend fun Flow<ExtractingDetails>.toResponse() = toList()
    .groupBy(ExtractingDetails::domainId)
    .entries.single().let { (domainId, extractingDetails) ->
        AddExtractingDetailsResponse(
            domainId = domainId,
            extractedFieldsDetails = extractingDetails.map { it.toExtractingDetailsResponse() }
        )
    }

private fun ExtractedProperty.toResponsePair() =
    when (this) {
        is Attribute -> ExtractedPropertyType.ATTRIBUTE to attributeName
        is OwnText -> ExtractedPropertyType.OWN_TEXT to null
        is Text -> ExtractedPropertyType.TEXT to null
    }

private fun SelectorInResponse.toEntityModel() = Selector(
    value = value,
    index = index
)

private fun extractedPropertyFrom(extractedPropertyType: ExtractedPropertyType, extractedPropertyValue: String?) =
    when (extractedPropertyType) {
        ExtractedPropertyType.TEXT -> Text
        ExtractedPropertyType.OWN_TEXT -> OwnText
        ExtractedPropertyType.ATTRIBUTE -> Attribute(
            extractedPropertyValue
                ?: throw InvalidInputException("No value property value provided for Attribute property type")
        )
    }
