package com.shykial.kScrapperCore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScrapperCore.helper.Given
import com.shykial.kScrapperCore.helper.RestTest
import com.shykial.kScrapperCore.helper.Then
import com.shykial.kScrapperCore.helper.When
import com.shykial.kScrapperCore.helper.assertFieldsToBeEqual
import com.shykial.kScrapperCore.helper.decodeBase64
import com.shykial.kScrapperCore.helper.extractingBody
import com.shykial.kScrapperCore.helper.saveAllIn
import com.shykial.kScrapperCore.helper.saveIn
import com.shykial.kScrapperCore.helper.toBase64String
import com.shykial.kScrapperCore.mapper.toEntities
import com.shykial.kScrapperCore.mapper.toExtractingDetailsResponse
import com.shykial.kScrapperCore.model.entity.Attribute
import com.shykial.kScrapperCore.model.entity.ExtractingDetails
import com.shykial.kScrapperCore.model.entity.OwnText
import com.shykial.kScrapperCore.model.entity.RegexReplacement
import com.shykial.kScrapperCore.model.entity.Selector
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import com.shykial.kScrapperCore.starter.MongoDBStarter
import generated.com.shykial.kScrapperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScrapperCore.models.ExtractedPropertyType
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsUpdateRequest
import io.restassured.http.ContentType
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.random.Random
import kotlin.random.nextInt
import generated.com.shykial.kScrapperCore.models.RegexReplacement as RegexReplacementInApi
import generated.com.shykial.kScrapperCore.models.Selector as SelectorInApi

@SpringBootTest
internal class ExtractingDetailsControllerTest(
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
) : RestTest(), MongoDBStarter {

    init {
        RestAssuredWebTestClient.basePath = "/extracting-details"
    }

    @BeforeEach
    fun setup() {
        runBlocking { extractingDetailsRepository.deleteAll() }
    }

    @Nested
    inner class PositiveOutcome {

        @Test
        fun `should properly add extracting details on POST request`() = runTest {
            val request = sampleExtractingDetailsRequest
            extractingDetailsRepository.findByDomainId(request.domainId).let {
                assertThat(it).isEmpty()
            }

            Given {
                contentType(ContentType.JSON)
                body(sampleExtractingDetailsRequest.toJsonString())
            } When {
                post()
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<AddExtractingDetailsResponse> { response ->
                    assertThat(response.domainId).isEqualTo(request.domainId)
                    val entities = extractingDetailsRepository.findByDomainId(request.domainId)
                    assertThat(response.extractedFieldsDetails).hasSameElementsAs(entities.map { it.toExtractingDetailsResponse() })
                }
            }
        }

        @Test
        fun `should properly retrieve extracting details by domainId on GET request`() = runTest {
            val entities = sampleExtractingDetailsRequest
                .toEntities()
                .saveAllIn(extractingDetailsRepository)
            val domainId = sampleExtractingDetailsRequest.domainId

            Given {
                queryParam("domainId", domainId)
            } When {
                get()
            } Then {
                status(HttpStatus.OK)
                extractingBody<List<ExtractingDetailsResponse>> { response ->
                    assertThat(response).hasSameElementsAs(entities.map { it.toExtractingDetailsResponse() })
                }
            }
        }

        @Test
        fun `should properly retrieve extracting details by ID on GET request`() = runTest {
            val entity = sampleExtractingDetailsRequest.toEntities().random()
                .saveIn(extractingDetailsRepository)

            When {
                get("/${entity.id}")
            } Then {
                status(HttpStatus.OK)
                extractingBody<ExtractingDetailsResponse> {
                    assertThat(it).isEqualTo(entity.toExtractingDetailsResponse())
                }
            }
        }

        @Test
        fun `should properly retrieve extracting details by domainId and fieldNames on GET request`() = runTest {
            val allEntities = sampleExtractingDetailsRequest.toEntities()
                .saveAllIn(extractingDetailsRepository)
            val domainId = sampleExtractingDetailsRequest.domainId
            val randomEntities = allEntities.shuffled().take(3)

            Given {
                queryParam("domainId", domainId)
                queryParam("fieldNames", randomEntities.map { it.fieldName })
            } When {
                get()
            } Then {
                status(HttpStatus.OK)
                extractingBody<List<ExtractingDetailsResponse>> { response ->
                    assertThat(response).hasSameElementsAs(randomEntities.map { it.toExtractingDetailsResponse() })
                }
            }
        }

        @Test
        fun `should properly update extracting details on PUT request`() = runTest {
            val initialRegexString = sampleRegexString()
            val initialExtractingDetails = ExtractingDetails(
                domainId = "sampleDomainId",
                fieldName = "sampleFieldName",
                selector = Selector(value = "sampleSelector", index = 0),
                extractedProperty = OwnText,
                regexReplacements = mutableListOf(
                    RegexReplacement(initialRegexString.toRegex(), "replacement")
                )
            ).saveIn(extractingDetailsRepository)
            val updateRequest = ExtractingDetailsUpdateRequest(
                fieldName = initialExtractingDetails.fieldName,
                selector = with(initialExtractingDetails.selector) { SelectorInApi(value = value, index = index + 1) },
                extractedPropertyType = ExtractedPropertyType.ATTRIBUTE,
                extractedPropertyValue = "newValue",
                regexReplacements = listOf(
                    RegexReplacementInApi(initialRegexString.toBase64String(), "replacement"),
                    RegexReplacementInApi("newRegex".toBase64String(), "newReplacement")
                )
            )

            Given {
                contentType(ContentType.JSON)
                body(updateRequest.toJsonString())
            } When {
                put("/${initialExtractingDetails.id}")
            } Then {
                status(HttpStatus.NO_CONTENT)

                extractingDetailsRepository.findById(initialExtractingDetails.id)!!.run {
                    assertFieldsToBeEqual(
                        fieldName to updateRequest.fieldName,
                        selector.index to updateRequest.selector.index,
                        selector.value to updateRequest.selector.value,
                        extractedProperty to Attribute(updateRequest.extractedPropertyValue!!),
                    )
                    assertThat(regexReplacements)
                        .usingComparatorForType(regexComparator, RegexReplacement::class.java)
                        .isEqualTo(updateRequest.regexReplacements?.toListInEntity())
                }
            }
        }
    }
}

private val sampleExtractingDetailsRequest = ExtractingDetailsRequest(
    domainId = "sampleDomainId",
    extractedFieldsDetails = List(Random.nextInt(5..10)) {
        val extractedPropertyType = ExtractedPropertyType.values().random()
        ExtractedFieldDetails(
            fieldName = randomAlphabetic(10),
            selector = SelectorInApi(value = randomAlphabetic(4), index = Random.nextInt(2)),
            extractedPropertyType = extractedPropertyType,
            extractedPropertyValue = extractedPropertyType.let {
                if (it == ExtractedPropertyType.ATTRIBUTE) randomAlphabetic(10) else null
            },
            regexReplacements = List(Random.nextInt(0..5)) {
                RegexReplacementInApi(sampleRegexString().toBase64String(), randomAlphabetic(10))
            }
        )
    }
)

private val regexComparator = Comparator<RegexReplacement> { first, second ->
    first.regex.pattern compareTo second.regex.pattern
}.then { first, second -> first.replacement compareTo second.replacement }

private fun sampleRegexString() = """\w+(${randomAlphanumeric(5)}){3,}(?<=${randomAlphanumeric(4)})"""

private fun List<RegexReplacementInApi>.toListInEntity() = map {
    RegexReplacement(decodeBase64(it.base64EncodedRegex).toRegex(), it.replacement)
}
