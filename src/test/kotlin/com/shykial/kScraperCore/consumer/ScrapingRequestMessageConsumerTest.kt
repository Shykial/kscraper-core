package com.shykial.kScraperCore.consumer

import com.ninjasquad.springmockk.SpykBean
import com.shykial.kScraperCore.configuration.rabbitmq.createAvroDeserializer
import com.shykial.kScraperCore.configuration.rabbitmq.createAvroSerializer
import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.service.ScrapingService
import com.shykial.kScraperCore.starter.RequiredServicesStarter
import generated.com.shykial.kScraperCore.avro.ResourceScrapingResult
import generated.com.shykial.kScraperCore.avro.ScrapingRequestMessageAvro
import generated.com.shykial.kScraperCore.avro.ScrapingResponseMessageAvro
import generated.com.shykial.kScraperCore.avro.ScrapingSuccess
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import generated.com.shykial.kScraperCore.avro.ScrapedResource as ScrapedResourceInAvro

@SpringBootTest
class ScrapingRequestMessageConsumerTest(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.exchange.scraping}") private val scrapingExchange: String,
    @Value("\${rabbitmq.consumer.queue.scraping-request}") private val scrapingRequestQueue: String,
    @Value("\${rabbitmq.producer.queue.scraping-response}") private val scrapingResponseQueue: String
) : RequiredServicesStarter {

    @SpykBean
    private lateinit var scrapingService: ScrapingService

    private val scrapingRequestSerializer = createAvroSerializer<ScrapingRequestMessageAvro>()
    private val scrapingResponseDeserializer =
        createAvroDeserializer<ScrapingResponseMessageAvro>(ScrapingResponseMessageAvro.getClassSchema())

    @BeforeEach
    fun clearMocks() {
        clearAllMocks()
    }

    @Test
    fun `should properly process scraping request message send received on queue and send scraping response message`() =
        runTest {
            // given
            val request = createSampleScrapingRequestAvroMessage()
            val mockedScrapedDataList = mockScrapingServiceIndividualCalls(request)

            // when
            sendAvroMessageToScrapingRequestQueue(request)

            // then
            rabbitTemplate.receive(scrapingResponseQueue, 5.seconds.inWholeMilliseconds)!!
                .body
                .run(scrapingResponseDeserializer::deserialize)
                .shouldBeComposedOfProperData(request, mockedScrapedDataList)
        }

    private fun ScrapingResponseMessageAvro.shouldBeComposedOfProperData(
        request: ScrapingRequestMessageAvro,
        mockedScrapedDataList: List<ScrapedData>
    ) {
        requestId shouldBe request.requestId
        scrapingResults shouldContainExactly mockedScrapedDataList.map {
            ResourceScrapingResult(
                it.url,
                ScrapingSuccess(
                    it.scrapedFields.mapKeys { (key, _) -> key.fieldName },
                    emptyList(),
                    it.timestamp
                )
            )
        }
    }

    private fun sendAvroMessageToScrapingRequestQueue(request: ScrapingRequestMessageAvro) {
        rabbitTemplate.send(
            scrapingExchange,
            scrapingRequestQueue,
            Message(scrapingRequestSerializer.serialize(request))
        )
    }

    private fun createSampleScrapingRequestAvroMessage() = ScrapingRequestMessageAvro(
        UUID.randomUUID().toString(),
        listOf(
            ScrapedResourceInAvro("sampleResource1.com/1", listOf("name", "price")),
            ScrapedResourceInAvro("sampleResource2.com/2", listOf("test"))
        )
    )

    private fun mockScrapingServiceIndividualCalls(request: ScrapingRequestMessageAvro) =
        request.scrapedResources.map { scrapedResource ->
            ScrapedData(
                url = scrapedResource.url,
                scrapedFields = scrapedResource.fields
                    ?.associate { field ->
                        mockk<ExtractingDetails> {
                            every { fieldName } returns field
                        } to "${field}_scraped_value"
                    }.orEmpty(),
                failedDetails = listOf()
            ).also { mockedResponse ->
                coEvery {
                    scrapingService.scrapeUrl(scrapedResource.url, scrapedResource.fields)
                } returns mockedResponse
            }
        }
}
