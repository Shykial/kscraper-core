package com.shykial.kScraperCore.helper

import com.shykial.kScraperCore.starter.MockServerStarter
import org.mockserver.client.ForwardChainExpectation
import org.mockserver.mock.Expectation
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

inline fun toRequest(
    requestSetup: HttpRequest.() -> Unit
): ForwardChainExpectation = MockServerStarter.mockServerClient.`when`(HttpRequest().apply(requestSetup))

inline infix fun ForwardChainExpectation.respond(
    responseSetup: HttpResponse.() -> Unit
): Array<Expectation> = respond(HttpResponse().apply(responseSetup))

context(HttpRequest)
fun Map<String, String>.toMockServerHeaders(): List<Header> = map { Header(it.key, it.value) }
