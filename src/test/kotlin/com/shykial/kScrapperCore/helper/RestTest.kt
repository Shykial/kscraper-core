package com.shykial.kScrapperCore.helper

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import javax.annotation.PostConstruct

@AutoConfigureWebTestClient
abstract class RestTest {
    abstract val objectMapper: ObjectMapper
    abstract val webTestClient: WebTestClient

    @PostConstruct
    private fun setupRestAssured() {
        RestAssuredWebTestClient.webTestClient(webTestClient)
    }

    fun Any?.toJsonString(): String = objectMapper.writeValueAsString(this)
}
