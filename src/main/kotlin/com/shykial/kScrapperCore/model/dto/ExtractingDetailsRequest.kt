package com.shykial.kScrapperCore.model.dto

data class ExtractingDetailsRequest(
    val domainId: String,
    val extractedFieldDetails: List<ExtractedFieldDetails>,
)

data class ExtractedFieldDetails(
    val fieldName: String,
    val selector: SelectorRequest,
    val extractedPropertyType: ExtractedPropertyType,
    val extractedPropertyValue: String? = null,
    val base64EncodedRegexReplacements: Map<String, String>? = null,
)

data class SelectorRequest(
    val value: String,
    val index: Int,
)

enum class ExtractedPropertyType {
    TEXT, OWN_TEXT, ATTRIBUTE
}
