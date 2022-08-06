package com.shykial.kScraperCore.starter

import mu.KotlinLogging
import org.junit.jupiter.api.BeforeAll
import org.mockserver.client.MockServerClient
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.utility.DockerImageName

private val log = KotlinLogging.logger { }

interface MockServerStarter {
    @BeforeAll
    fun assureStarted() {
        log.info("MockServer starting, dashboard url: $mockServerUrl/mockservers/dashboard")
    }

    companion object {
        private val mockServerContainer = MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"))
            .apply { start() }
        val mockServerClient = MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)
        val mockServerUrl: String = mockServerContainer.endpoint
    }
}
