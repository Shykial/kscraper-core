package com.shykial.kScraperCore.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.shykial.kScraperCore.init.UsersInitializer
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.security.JwtProperties
import io.restassured.config.HeaderConfig
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "PT20S")
annotation class KScraperRestTest

interface RestTest {
    val objectMapper: ObjectMapper
    val webTestClient: WebTestClient

    @BeforeAll
    fun setupRestAssured() {
        RestAssuredWebTestClient.webTestClient(webTestClient)
    }

    fun WebTestClientRequestSpecification.jsonBody(body: Any?): WebTestClientRequestSpecification =
        contentType(ContentType.JSON)
            .body(body.toJsonString())

    private fun Any?.toJsonString(): String = objectMapper.writeValueAsString(this)
}

interface RestTestWithAdminAuthentication : RestTest {
    val usersInitializer: UsersInitializer

    @BeforeAll
    fun setupAdminAuthentication() = runTest {
        usersInitializer.assureUsersPresentInDB()
        RestAssuredWebTestClient.requestSpecification = Given {
            adminAuthHeader()
        }.config(
            RestAssuredWebTestClient.config()
                .headerConfig(HeaderConfig.headerConfig().overwriteHeadersWithName(HttpHeaders.AUTHORIZATION))
        )
    }

    fun WebTestClientRequestSpecification.adminAuthHeader(
        adminLogin: String? = null
    ): WebTestClientRequestSpecification = header(
        Header(
            HttpHeaders.AUTHORIZATION,
            usersInitializer.getUserJwtToken(
                userRole = UserRole.ADMIN,
                userLogin = adminLogin
            ).let { JwtProperties.AUTH_HEADER_PREFIX + it.token }
        )
    )

    fun WebTestClientRequestSpecification.apiUserAuthHeader(
        userLogin: String? = null
    ): WebTestClientRequestSpecification = header(
        Header(
            HttpHeaders.AUTHORIZATION,
            usersInitializer.getUserJwtToken(
                userRole = UserRole.API_USER,
                userLogin = userLogin
            ).let { JwtProperties.AUTH_HEADER_PREFIX + it.token }
        )
    )
}
