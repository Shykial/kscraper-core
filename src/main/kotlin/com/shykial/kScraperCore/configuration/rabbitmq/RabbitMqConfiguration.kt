package com.shykial.kScraperCore.configuration.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfiguration(
    @Value("\${rabbitmq.consumer.exchange}") private val exchangeName: String,
    @Value("\${rabbitmq.consumer.queue.scraping-request}") private val scrapingQueue: String,
    private val objectMapper: ObjectMapper
) {
    @Bean
    fun kScraperCoreExchange(): DirectExchange = DirectExchange(exchangeName)

    @Bean
    fun scrapingRequestsQueue(): Queue = Queue(scrapingQueue)

    @Bean
    fun binding(queue: Queue, exchange: DirectExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).withQueueName()

    @Bean
    fun jackson2MessageConverter(): MessageConverter = Jackson2JsonMessageConverter(objectMapper)
}
