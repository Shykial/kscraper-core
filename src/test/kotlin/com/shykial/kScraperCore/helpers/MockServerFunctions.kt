package com.shykial.kScraperCore.helpers

import com.shykial.kScraperCore.starter.MockServerStarter
import com.shykial.kScraperCore.starter.MockServerStarter.Companion.mockServerClient
import org.mockserver.client.ForwardChainExpectation
import org.mockserver.mock.Expectation
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

context(MockServerStarter)
inline fun toRequest(
    requestSetup: HttpRequest.() -> Unit
): ForwardChainExpectation = mockServerClient.`when`(HttpRequest().apply(requestSetup))

inline infix fun ForwardChainExpectation.respond(
    responseSetup: HttpResponse.() -> Unit
): Array<Expectation> = respond(HttpResponse().apply(responseSetup))
