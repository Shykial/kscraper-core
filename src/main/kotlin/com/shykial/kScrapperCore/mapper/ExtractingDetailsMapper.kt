package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.exception.InvalidInputException
import com.shykial.kScrapperCore.model.dto.ExtractedPropertyType
import com.shykial.kScrapperCore.model.dto.ExtractingDetailsRequest
import com.shykial.kScrapperCore.model.dto.SelectorRequest
import com.shykial.kScrapperCore.model.entity.*
import org.bson.internal.Base64

fun ExtractingDetailsRequest.toEntities() = extractedFieldDetails.map {
    ExtractingDetails(
        domainId = domainId,
        fieldName = it.fieldName,
        selector = it.selector.toEntityModel(),
        extractedProperty = extractedPropertyFrom(it.extractedPropertyType, it.extractedPropertyValue),
        regexReplacements = it.base64EncodedRegexReplacements?.map { (key, value) ->
            RegexReplacement(
                regex = Regex(String(Base64.decode(key))),
                replacement = value
            )
        }
    )
}

private fun SelectorRequest.toEntityModel() = Selector(
    value = value,
    index = index
)

private fun extractedPropertyFrom(extractedPropertyType: ExtractedPropertyType, extractedPropertyValue: String?) =
    when (extractedPropertyType) {
        ExtractedPropertyType.TEXT -> Text
        ExtractedPropertyType.OWN_TEXT -> OwnText
        ExtractedPropertyType.ATTRIBUTE -> Attribute(extractedPropertyValue
            ?: throw InvalidInputException("No value property value provided for Attribute property type")
        )
    }