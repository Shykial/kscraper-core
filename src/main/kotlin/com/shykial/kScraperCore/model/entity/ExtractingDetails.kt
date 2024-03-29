package com.shykial.kScraperCore.model.entity

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ExtractingDetails(
    @Indexed
    val domainId: String,
    var fieldName: String,
    var selector: Selector,
    var extractedProperty: ExtractedProperty,
    var regexFilter: Regex? = null,
    var regexReplacements: MutableList<RegexReplacement>? = RegexReplacement.DEFAULTS.toMutableList()
) : BaseDocument() {
    var currentScrapeFailures: Int = 0
}

sealed interface ExtractedProperty {
    object Text : ExtractedProperty
    object OwnText : ExtractedProperty
    object Html : ExtractedProperty
    data class Attribute(val attributeName: String) : ExtractedProperty
}

data class Selector(
    val value: String,
    val index: Int = 0
)

data class RegexReplacement(val regex: Regex, val replacement: String) {
    companion object {
        private val PRICE_FILTER_REGEX = Regex("""[^\d,.]""")

        val DEFAULTS = listOf(
            RegexReplacement(PRICE_FILTER_REGEX, ""),
            RegexReplacement(Regex(","), ".")
        )
    }
}
