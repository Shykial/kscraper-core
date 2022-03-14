package com.shykial.kScrapperCore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScrapperCore.helpers.Given
import com.shykial.kScrapperCore.helpers.RestTest
import com.shykial.kScrapperCore.helpers.Then
import com.shykial.kScrapperCore.helpers.When
import com.shykial.kScrapperCore.helpers.awaitAndAssertEmpty
import com.shykial.kScrapperCore.helpers.extractingBody
import com.shykial.kScrapperCore.repository.ExtractingDetailsRepository
import com.shykial.kScrapperCore.starter.MongoDBStarter
import generated.com.shykial.kScrapperCore.models.ExtractedFieldDetails
import generated.com.shykial.kScrapperCore.models.ExtractedPropertyType
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsRequest
import generated.com.shykial.kScrapperCore.models.ExtractingDetailsResponse
import generated.com.shykial.kScrapperCore.models.Selector
import io.restassured.http.ContentType
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.bson.internal.Base64
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.random.Random
import kotlin.random.nextInt

@SpringBootTest
internal class ExtractingDetailsControllerTest(
    override val webTestClient: WebTestClient,
    override val objectMapper: ObjectMapper,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
) : RestTest(), MongoDBStarter {

    init {
        RestAssuredWebTestClient.basePath = "/extracting-details"
    }

    @Nested
    inner class PositiveOutcome {
        @Test
        fun `should properly add extracting details request`() = runTest {
            val request = sampleExtractingDetailsRequest
            extractingDetailsRepository.findByDomainId(request.domainId).awaitAndAssertEmpty()
            Given {
                contentType(ContentType.JSON)
                body(sampleExtractingDetailsRequest.toJsonString())
            } When {
                post()
            } Then {
                status(HttpStatus.CREATED)
                extractingBody<ExtractingDetailsResponse> {
                    println()
                }
            }
        }
    }
}

private val sampleExtractingDetailsRequest = ExtractingDetailsRequest(
    domainId = "sampleDomainId",
    extractedFieldsDetails = List(Random.nextInt(2..5)) {
        val extractedPropertyType = ExtractedPropertyType.values().random()
        ExtractedFieldDetails(fieldName = randomAlphabetic(10),
            selector = Selector(value = randomAlphabetic(4), index = Random.nextInt(2)),
            extractedPropertyType = extractedPropertyType,
            extractedPropertyValue = extractedPropertyType.let {
                if (it == ExtractedPropertyType.ATTRIBUTE) randomAlphabetic(10) else null
            },
            base64EncodedRegexReplacements = (0..Random.nextInt(0..5)).associate {
                randomAlphanumeric(10).toBase64String() to randomAlphabetic(10)
            }
        )
    }
)

private fun String.toBase64String() = Base64.encode(toByteArray())