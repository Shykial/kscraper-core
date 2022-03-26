package com.shykial.kScrapperCore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScrapperCore.helpers.Given
import com.shykial.kScrapperCore.helpers.RestTest
import com.shykial.kScrapperCore.helpers.Then
import com.shykial.kScrapperCore.helpers.When
import com.shykial.kScrapperCore.helpers.awaitAndAssertEmpty
import com.shykial.kScrapperCore.helpers.extractingBody
import com.shykial.kScrapperCore.helpers.toBase64String
import com.shykial.kScrapperCore.mapper.toEntities
import com.shykial.kScrapperCore.mapper.toExtractingDetailsResponse
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import com.shykial.kScrapperCore.starter.MongoDBStarter
import generated.com.shykial.kScrapperCore.models.AddExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScrapperCore.models.ExtractedPropertyType
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.Selector
import io.restassured.http.ContentType
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.random.Random
import kotlin.random.nextInt

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
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
        extractingDetailsRepository.deleteAll().block()
    }

    @Nested
    inner class PositiveOutcome {

        @Test
        fun `should properly add extracting details on POST request`() = runTest {
            val request = sampleExtractingDetailsRequest
            extractingDetailsRepository.findByDomainId(request.domainId).awaitAndAssertEmpty()
            Given {
                contentType(ContentType.JSON)
                body(sampleExtractingDetailsRequest.toJsonString())
            } When {
                post()
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<AddExtractingDetailsResponse> { response ->
                    assertThat(response.domainId).isEqualTo(request.domainId)
                    val entities = extractingDetailsRepository.findByDomainId(request.domainId).collectList().block()!!
                    assertThat(response.extractedFieldsDetails).hasSameElementsAs(entities.map { it.toExtractingDetailsResponse() })
                }
            }
        }

        @Test
        fun `should properly retrieve extracting details by domainId on GET request`() = runTest {
            val entities = sampleExtractingDetailsRequest.toEntities()
            val domainId = sampleExtractingDetailsRequest.domainId
            extractingDetailsRepository.saveAll(entities).collectList().awaitSingle()
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
        fun `should properly retrieve extracting details by domainId and fieldNames on GET request`() = runTest {
            val allEntities = sampleExtractingDetailsRequest.toEntities()
            val domainId = sampleExtractingDetailsRequest.domainId
            extractingDetailsRepository.saveAll(allEntities).collectList().awaitSingle()
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
    }
}

private val sampleExtractingDetailsRequest = ExtractingDetailsRequest(
    domainId = "sampleDomainId",
    extractedFieldsDetails = List(Random.nextInt(5..10)) {
        val extractedPropertyType = ExtractedPropertyType.values().random()
        ExtractedFieldDetails(
            fieldName = randomAlphabetic(10),
            selector = Selector(value = randomAlphabetic(4), index = Random.nextInt(2)),
            extractedPropertyType = extractedPropertyType,
            extractedPropertyValue = extractedPropertyType.let {
                if (it == ExtractedPropertyType.ATTRIBUTE) randomAlphabetic(10) else null
            },
            base64EncodedRegexReplacements = (0..Random.nextInt(0..5)).associate {
                sampleRegex().toBase64String() to randomAlphabetic(10)
            }
        )
    }
)

private fun sampleRegex() = """\w+(${randomAlphanumeric(5)}){3,}(?<=${randomAlphanumeric(4)})"""
