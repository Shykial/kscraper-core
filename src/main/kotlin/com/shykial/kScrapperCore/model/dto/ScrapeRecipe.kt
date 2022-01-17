package com.shykial.kScrapperCore.model.dto

data class ScrapeRecipeRequest(
    val domainName: String,
    val extractingDetails: Map<String, ExtractingDetailsRequest>
)

data class ExtractingDetailsRequest(
    val selector: SelectorRequest,
    val extractedPropertyType: ExtractedPropertyType,
    val extractedPropertyValue: String? = null,
    val regexReplacements: Map<String, String>? = null
)

data class SelectorRequest(
    val value: String,
    val index: Int,
)

enum class ExtractedPropertyType {
    TEXT, OWN_TEXT, ATTRIBUTE
}
