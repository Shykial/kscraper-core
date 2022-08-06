package com.shykial.kScraperCore.helper

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.http.ContentType
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import javax.annotation.PostConstruct

@AutoConfigureWebTestClient(timeout = "PT20S")
abstract class RestTest {
    abstract val objectMapper: ObjectMapper
    abstract val webTestClient: WebTestClient

    @PostConstruct
    private fun setupRestAssured() {
        RestAssuredWebTestClient.webTestClient(webTestClient)
    }

    fun WebTestClientRequestSpecification.jsonBody(body: Any?): WebTestClientRequestSpecification =
        contentType(ContentType.JSON)
            .body(body.toJsonString())

    private fun Any?.toJsonString(): String = objectMapper.writeValueAsString(this)
}
