package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.exception.InvalidInputException
import com.shykial.kScrapperCore.model.entity.Attribute
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.model.entity.OwnText
import com.shykial.kScrapperCore.model.entity.RegexReplacement
import com.shykial.kScrapperCore.model.entity.Selector
import com.shykial.kScrapperCore.model.entity.Text
import generated.com.shykial.kScrapperCore.models.ExtractedPropertyType
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import org.bson.internal.Base64
import generated.com.shykial.kScrapperCore.models.Selector as SelectorRequest

fun ExtractingDetailsRequest.toEntities() = extractedFieldsDetails.map {
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
        ExtractedPropertyType.ATTRIBUTE -> Attribute(
            extractedPropertyValue
                ?: throw InvalidInputException("No value property value provided for Attribute property type")
        )
    }
