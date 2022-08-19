package com.shykial.kScraperCore.configuration.rabbitmq

import generated.com.shykial.kScraperCore.avro.ScrapingRequestMessageAvro
import generated.com.shykial.kScraperCore.avro.ScrapingResponseMessageAvro
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfiguration(
    @Value("\${rabbitmq.exchange.scraping}") private val scrapingExchange: String,
    @Value("\${rabbitmq.consumer.queue.scraping-request}") private val scrapingRequestQueue: String,
    @Value("\${rabbitmq.producer.queue.scraping-response}") private val scrapingResponseQueue: String
) {
    @Bean
    fun scrapingExchange(): DirectExchange = DirectExchange(scrapingExchange)

    @Bean
    fun scrapingRequestsQueue(): Queue = Queue(scrapingRequestQueue)

    @Bean
    fun scrapingResponseQueue(): Queue = Queue(scrapingResponseQueue)

    @Bean
    fun bindings(
        queues: List<Queue>,
        exchange: DirectExchange
    ): Declarables = queues
        .map { BindingBuilder.bind(it).to(exchange).withQueueName() }
        .run(::Declarables)

    @Bean
    fun scrapingRequestDeserializer(): AvroDeserializer<ScrapingRequestMessageAvro> =
        createAvroDeserializer(ScrapingRequestMessageAvro.getClassSchema())

    @Bean
    fun scrapingResponseSerializer(): AvroSerializer<ScrapingResponseMessageAvro> = createAvroSerializer()
}
