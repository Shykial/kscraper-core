package com.shykial.kScraperCore.producer

import com.shykial.kScraperCore.configuration.rabbitmq.AvroSerializer
import generated.com.shykial.kScraperCore.avro.ScrapingResponseMessageAvro
import mu.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ScrapingResponseMessagesProducer(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.exchange.scraping}") private val producerExchange: String,
    @Value("\${rabbitmq.producer.queue.scraping-response}") private val scrapingResponseQueue: String,
    private val serializer: AvroSerializer<ScrapingResponseMessageAvro>
) {
    private val log = KotlinLogging.logger { }

    fun publishScrapingResponseMessage(response: ScrapingResponseMessageAvro) {
        log.info(
            """Sending ScrapingResponseMessage: $response
               to exchange $producerExchange
               queue: $scrapingResponseQueue"
            """.trimIndent()
        )
        rabbitTemplate.send(
            producerExchange,
            scrapingResponseQueue,
            Message(serializer.serialize(response))
        )
    }
}
