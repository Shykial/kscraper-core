package com.shykial.kScraperCore.mocks

import io.mockk.clearConstructorMockk
import io.mockk.every
import io.mockk.mockkConstructor
import it.skrape.fetcher.Request
import org.junit.jupiter.api.AfterEach

interface HttpMocker {
    fun mockHttpRequestCallUrl(originalUrl: String, mockedUrl: String)

    @AfterEach
    fun clearMock()
}

class SkrapeItCallMocker : HttpMocker {
    override fun mockHttpRequestCallUrl(originalUrl: String, mockedUrl: String) {
        mockkConstructor(Request::class)
        every { anyConstructed<Request>().url } returns mockedUrl
    }

    override fun clearMock() {
        clearConstructorMockk(Request::class)
    }
}
