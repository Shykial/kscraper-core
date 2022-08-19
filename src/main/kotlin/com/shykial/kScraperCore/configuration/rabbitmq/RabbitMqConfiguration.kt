package com.shykial.kScraperCore.configuration.rabbitmq

import generated.com.shykial.kScraperCore.avro.ScrapingRequestMessageAvro
import generated.com.shykial.kScraperCore.avro.ScrapingResponseMessageAvro
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfiguration(
    @Value("\${rabbitmq.exchange.scraping}") private val exchangeName: String,
    @Value("\${rabbitmq.consumer.queue.scraping-request}") private val scrapingQueue: String
) {
    @Bean
    fun kScraperCoreExchange(): DirectExchange = DirectExchange(exchangeName)

    @Bean
    fun scrapingRequestsQueue(): Queue = Queue(scrapingQueue)

    @Bean
    fun binding(queue: Queue, exchange: DirectExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).withQueueName()

    @Bean
    fun scrapingRequestDeserializer(): AvroDeserializer<ScrapingRequestMessageAvro> =
        createDeserializer(ScrapingRequestMessageAvro.getClassSchema())

    @Bean
    fun scrapingResponseSerializer(): AvroSerializer<ScrapingResponseMessageAvro> = createSerializer()
}
