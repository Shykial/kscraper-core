package com.shykial.kScraperCore.producer

import generated.com.shykial.kScraperCore.avro.ScrapingResponseMessageAvro
import mu.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ScrapingResponseMessagesProducer(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.producer.exchange}") private val producerExchange: String,
    @Value("\${rabbitmq.producer.queue.scraping-response}") private val scrapingResponseQueue: String
) {
    private val log = KotlinLogging.logger { }

    fun publishScrapingResponseMessage(response: ScrapingResponseMessageAvro) {
        log.info(
            """Sending ScrapingResponseMessage: $response
               to exchange $producerExchange
               queue: $scrapingResponseQueue"
            """.trimIndent()
        )
        rabbitTemplate.convertAndSend(
            producerExchange,
            scrapingResponseQueue,
            response
        )
    }
}
