@file:Suppress("TestFunctionName")

package com.shykial.kScrapperCore.helper

import io.restassured.common.mapper.TypeRef
import io.restassured.internal.ResponseSpecificationImpl
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import io.restassured.module.webtestclient.internal.ValidatableWebTestClientResponseImpl
import io.restassured.module.webtestclient.response.ValidatableWebTestClientResponse
import io.restassured.module.webtestclient.response.WebTestClientResponse
import io.restassured.module.webtestclient.specification.WebTestClientRequestSender
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification
import io.restassured.response.ExtractableResponse

fun Given(
    block: WebTestClientRequestSpecification.() -> WebTestClientRequestSpecification
): WebTestClientRequestSpecification = RestAssuredWebTestClient.given().run(block)

infix fun WebTestClientRequestSpecification.When(
    block: WebTestClientRequestSender.() -> WebTestClientResponse
): WebTestClientResponse = `when`().run(block)

fun When(
    block: WebTestClientRequestSender.() -> WebTestClientResponse
): WebTestClientResponse = RestAssuredWebTestClient.given().`when`().run(block)

infix fun <T> WebTestClientResponse.Extract(block: ExtractableResponse<WebTestClientResponse>.() -> T): T =
    then().extract().run(block)

fun <T> ValidatableWebTestClientResponse.Extract(
    block: ExtractableResponse<WebTestClientResponse>.() -> T
): T = extract().run(block)

inline fun <reified T> ValidatableWebTestClientResponse.extractingBody(block: (T) -> Unit) =
    extract().`as`(object : TypeRef<T>() {}).run(block)

infix fun WebTestClientResponse.Then(block: ValidatableWebTestClientResponse.() -> Unit) = then()
    .also(
        doIfValidatableResponseImpl {
            forceDisableEagerAssert()
        }
    )
    .also { it.log().all() }
    .apply(block)
    .also(
        doIfValidatableResponseImpl {
            forceValidateResponse()
        }
    )

private fun doIfValidatableResponseImpl(
    fn: ResponseSpecificationImpl.() -> Unit
): (ValidatableWebTestClientResponse) -> Unit = { resp ->
    if (resp is ValidatableWebTestClientResponseImpl) {
        fn(resp.responseSpec)
    }
}
