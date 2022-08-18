package com.shykial.kScraperCore.configuration.rabbitmq

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqListenerConfiguration(
    @Value("\${rabbitmq.exchange}") private val exchangeName: String,
    @Value("\${rabbitmq.consumer.scraping-requests-queue}") private val scrapingQueue: String
) {
    @Bean
    fun kScraperCoreExchange(): DirectExchange = DirectExchange(exchangeName)

    @Bean
    fun scrapingRequestsQueue(): Queue = Queue(scrapingQueue)

    @Bean
    fun binding(queue: Queue, exchange: DirectExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).withQueueName()
}
