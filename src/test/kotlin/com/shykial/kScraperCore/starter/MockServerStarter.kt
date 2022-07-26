package com.shykial.kScraperCore.starter

import org.mockserver.client.MockServerClient
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.utility.DockerImageName

interface MockServerStarter {
    companion object {
        private val mockServerContainer = MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"))
            .apply { start() }
        val mockServerClient = MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)
        val mockServerUrl: String = mockServerContainer.endpoint
    }
}
