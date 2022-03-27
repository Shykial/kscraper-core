package com.shykial.kScrapperCore.model.entity

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

private val PRICE_FILTER_REGEX = Regex("""[^\d,.]""")
private val DEFAULT_REGEX_REPLACEMENT = RegexReplacement(PRICE_FILTER_REGEX, "")

@Document
data class ExtractingDetails(
    @Indexed
    val domainId: String,
    var fieldName: String,
    var selector: Selector,
    var extractedProperty: ExtractedProperty,
    var regexReplacements: MutableList<RegexReplacement>? = mutableListOf(DEFAULT_REGEX_REPLACEMENT),
) : BaseDocument()

sealed interface ExtractedProperty

object Text : ExtractedProperty
object OwnText : ExtractedProperty
data class Attribute(val attributeName: String) : ExtractedProperty

data class Selector(
    val value: String,
    val index: Int = 0,
)

data class RegexReplacement(val regex: Regex, val replacement: String)
