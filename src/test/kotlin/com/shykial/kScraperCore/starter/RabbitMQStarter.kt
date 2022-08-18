package com.shykial.kScraperCore.starter

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

interface RabbitMQStarter {
    companion object {
        private val rabbitMQContainer = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.10.7"))
            .apply {
                start()
                withExposedPorts(5672)
            }

        @JvmStatic
        @DynamicPropertySource
        fun rabbitMQProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.host") { rabbitMQContainer.host }
            registry.add("spring.rabbitmq.port") { rabbitMQContainer.getMappedPort(5672) }
            registry.add("spring.rabbitmq.username") { rabbitMQContainer.adminUsername }
            registry.add("spring.rabbitmq.password") { rabbitMQContainer.adminPassword }
        }
    }
}
