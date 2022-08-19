package com.shykial.kScraperCore.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.model.ScrapingRequestMessage
import com.shykial.kScraperCore.service.ScrapingMessagesService
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ScrapingRequestsListener(
    private val scrapingMessagesService: ScrapingMessagesService,
    private val objectMapper: ObjectMapper
) {
    private val log = KotlinLogging.logger { }

    @RabbitListener(queues = ["\${rabbitmq.consumer.queue.scraping-request}"])
    fun consumerScrapingRequestMessage(message: Message) = mono {
        message.body
            .run { objectMapper.readValue<ScrapingRequestMessage>(this) }
            .also { log.info("Received ScrapingRequestMessage with content $it") }
            .runSuspend(scrapingMessagesService::handleScrapingRequestMessage)
    }
}
