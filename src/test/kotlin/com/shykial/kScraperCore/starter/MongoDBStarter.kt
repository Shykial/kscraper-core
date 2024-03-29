package com.shykial.kScraperCore.starter

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

private const val mongoProperties = "spring.data.mongodb"

interface MongoDBStarter {
    companion object {
        private val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:5.0.9"))
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun mongoDBProperties(registry: DynamicPropertyRegistry) {
            registry.add("$mongoProperties.uri") { mongoDBContainer.replicaSetUrl }
        }
    }
}
