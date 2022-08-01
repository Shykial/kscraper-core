package com.shykial.kScraperCore.helper.resource

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.shykial.kScraperCore.model.entity.ExtractedProperty
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.model.entity.RegexReplacement
import com.shykial.kScraperCore.model.entity.Selector
import org.intellij.lang.annotations.Language
import org.springframework.core.io.ClassPathResource

private val jsonResourcesObjectMapper = jacksonObjectMapper()

enum class SupportedDomain(
    val domainName: String,
    @Language("spring-resource-reference") headersFile: String,
    @Language("spring-resource-reference") responseMappingsFile: String,
    @Language("spring-resource-reference") extractingDetailsInstructionsFile: String
) {
    EURO_RTV_AGD(
        domainName = "euro.com.pl",
        headersFile = "proper-domain-headers/euro-rtv-domain-headers.json",
        responseMappingsFile = "response-scraping-mappings/euro-rtv-response-mappings.json",
        extractingDetailsInstructionsFile = "proper-extracting-details/euro-rtv-extracting-details.json"
    );

    val headers: Map<String, String> = ClassPathResource(headersFile).readAndDeserialize()
    val responseMappings: List<ResponseMapping> = ClassPathResource(responseMappingsFile)
        .readAndDeserialize<List<IntermediateResponseMapping>>()
        .map {
            ResponseMapping(
                htmlContent = classPathFileContent(it.htmlFile),
                expectedFieldValue = it.expectedFieldValue
            )
        }
    private val extractedDetails = classPathFileContent(extractingDetailsInstructionsFile)
        .run(::parseExtractingDetails)

    fun extractingDetails(domainId: String) = extractedDetails.copy(domainId = domainId)
}

private data class IntermediateResponseMapping(
    val htmlFile: String,
    val expectedFieldValue: String
)

data class ResponseMapping(
    val htmlContent: String,
    val expectedFieldValue: String
)

private fun classPathFileContent(path: String) = ClassPathResource(path).file.readText()

private inline fun <reified T> ClassPathResource.readAndDeserialize() =
    jsonResourcesObjectMapper.readValue<T>(file.readText())

private fun parseExtractingDetails(jsonString: String) = jsonString
    .run(jsonResourcesObjectMapper::readTree)
    .let { rootNode ->
        ExtractingDetails(
            domainId = "",
            fieldName = rootNode["fieldName"].asText(),
            selector = Selector(
                value = rootNode["selector"]["value"].asText(),
                index = rootNode["selector"]["index"]?.asInt() ?: 0
            ),
            extractedProperty = rootNode["extractedProperty"].let {
                when (val attributeName = it["name"].asText()) {
                    "TEXT" -> ExtractedProperty.Text
                    "OWN_TEXT" -> ExtractedProperty.OwnText
                    "HTML" -> ExtractedProperty.Html
                    "ATTRIBUTE" -> ExtractedProperty.Attribute(it["value"].asText())
                    else -> error("invalid attribute $attributeName")
                }
            },
            regexFilter = rootNode["regexFilter"]?.asText()?.toRegex(),
            regexReplacements = rootNode["regexReplacements"]?.map {
                RegexReplacement(
                    rootNode["regex"].asText().toRegex(),
                    rootNode["replacement"].asText()
                )
            }?.toMutableList()
        )
    }
