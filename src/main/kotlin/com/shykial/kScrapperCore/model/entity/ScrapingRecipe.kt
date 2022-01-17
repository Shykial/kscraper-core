package com.shykial.kScrapperCore.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ScrapingRecipe(
    @Indexed(unique = true)
    val domainName: String,
    val extractingDetails: Map<String, ExtractingDetails>,
    val requestHeaders: Map<String, String>? = null,
    val requestTimeout: Int? = null,
) {
    @Id
    var id: String? = null
}

data class ExtractingDetails(
    val selector: Selector,
    val extractedProperty: ExtractedProperty,
    val regexReplacements: List<RegexReplacement> = listOf(defaultRegexReplacement),
)

private val priceFilterRegex = Regex("""[^\d,.]""")
private val defaultRegexReplacement = RegexReplacement(priceFilterRegex, "")

sealed interface ExtractedProperty

object Text : ExtractedProperty
object OwnText : ExtractedProperty
class Attribute(val attributeName: String) : ExtractedProperty

data class Selector(
    val value: String,
    val index: Int = 0,
)

data class RegexReplacement(val regex: Regex, val replacement: String)
