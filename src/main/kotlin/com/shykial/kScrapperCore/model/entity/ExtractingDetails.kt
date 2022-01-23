package com.shykial.kScrapperCore.model.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ExtractingDetails(
    @Indexed
    val domainId: String,
    val fieldName: String,
    val selector: Selector,
    val extractedProperty: ExtractedProperty,
    val regexReplacements: List<RegexReplacement>? = listOf(defaultRegexReplacement),
) {
    @Id
    val id: String = ObjectId.get().toHexString()
}

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
