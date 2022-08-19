package com.shykial.kScraperCore.producer

import com.shykial.kScraperCore.mapper.toAvroMessage
import com.shykial.kScraperCore.model.ScrapingResponseMessage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ScrapingResponseMessagesProducer(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.producer.exchange}") private val producerExchange: String,
    @Value("\${rabbitmq.producer.queue.scraping-response}") private val scrapingResponseQueue: String
) {
    fun publishScrapingResponseMessage(response: ScrapingResponseMessage) {
        rabbitTemplate.convertAndSend(
            producerExchange,
            scrapingResponseQueue,
            response.toAvroMessage()
        )
    }
}
