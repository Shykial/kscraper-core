package com.shykial.kScraperCore.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.helper.Given
import com.shykial.kScraperCore.helper.RestTest
import com.shykial.kScraperCore.helper.Then
import com.shykial.kScraperCore.helper.When
import com.shykial.kScraperCore.helper.decodeBase64
import com.shykial.kScraperCore.helper.extractingBody
import com.shykial.kScraperCore.helper.saveAllIn
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.helper.toBase64String
import com.shykial.kScraperCore.helper.usingTypeComparator
import com.shykial.kScraperCore.mapper.toEntities
import com.shykial.kScraperCore.mapper.toExtractingDetailsResponse
import com.shykial.kScraperCore.model.entity.ExtractedProperty
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.model.entity.RegexReplacement
import com.shykial.kScraperCore.model.entity.Selector
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import com.shykial.kScraperCore.starter.MongoDBStarter
import generated.com.shykial.kScraperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScraperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScraperCore.models.ExtractedPropertyType
import generated.com.shykial.kScraperCore.models.ExtractedPropertyType.ATTRIBUTE
import generated.com.shykial.kScraperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScraperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScraperCore.models.ExtractingDetailsUpdateRequest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.restassured.module.webtestclient.RestAssuredWebTestClient
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
import generated.com.shykial.kScraperCore.models.RegexReplacement as RegexReplacementInApi
import generated.com.shykial.kScraperCore.models.Selector as SelectorInApi

@SpringBootTest
internal class ExtractingDetailsEndpointTest(
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    private val extractingDetailsRepository: ExtractingDetailsRepository
) : RestTest(), MongoDBStarter {

    init {
        RestAssuredWebTestClient.basePath = "/extracting-details"
    }

    @BeforeEach
    fun setup() = runTest {
        extractingDetailsRepository.deleteAll()
    }

    @Nested
    inner class PositiveOutcome {

        @Test
        fun `should properly add extracting details on POST request`() = runTest {
            val request = sampleExtractingDetailsRequest
            extractingDetailsRepository.findByDomainId(request.domainId).shouldBeEmpty()

            Given {
                jsonBody(sampleExtractingDetailsRequest)
            } When {
                post()
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<AddExtractingDetailsResponse> { response ->
                    response.domainId shouldBe request.domainId
                    val entities = extractingDetailsRepository.findByDomainId(request.domainId)
                    response.extractedFieldsDetails shouldContainExactlyInAnyOrder
                        entities.map { it.toExtractingDetailsResponse() }
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
                    response shouldContainExactlyInAnyOrder entities.map { it.toExtractingDetailsResponse() }
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
                    it shouldBe entity.toExtractingDetailsResponse()
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
                    response shouldContainExactlyInAnyOrder randomEntities.map { it.toExtractingDetailsResponse() }
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
                extractedProperty = ExtractedProperty.OwnText,
                regexReplacements = mutableListOf(
                    RegexReplacement(initialRegexString.toRegex(), "replacement")
                )
            ).saveIn(extractingDetailsRepository)
            val updateRequest = ExtractingDetailsUpdateRequest(
                fieldName = initialExtractingDetails.fieldName,
                selector = with(initialExtractingDetails.selector) { SelectorInApi(value = value, index = index + 1) },
                extractedPropertyType = ATTRIBUTE,
                extractedPropertyValue = "newValue",
                regexReplacements = listOf(
                    RegexReplacementInApi(initialRegexString.toBase64String(), "replacement"),
                    RegexReplacementInApi("newRegex".toBase64String(), "newReplacement")
                )
            )

            Given {
                jsonBody(updateRequest)
            } When {
                put("/${initialExtractingDetails.id}")
            } Then {
                status(HttpStatus.NO_CONTENT)

                extractingDetailsRepository.findById(initialExtractingDetails.id)!!.run {
                    fieldName shouldBe updateRequest.fieldName
                    selector.index shouldBe updateRequest.selector.index
                    selector.value shouldBe updateRequest.selector.value
                    extractedProperty shouldBe ExtractedProperty.Attribute(updateRequest.extractedPropertyValue!!)
                    assertThat(regexReplacements)
                        .usingTypeComparator(regexComparator)
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
            extractedPropertyValue = randomAlphabetic(10).takeIf { extractedPropertyType == ATTRIBUTE },
            regexFilter = sampleRegexString().toBase64String(),
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
    RegexReplacement(
        regex = it.base64EncodedRegex.decodeBase64().toRegex(),
        replacement = it.replacement
    )
}
