package com.shykial.kScraperCore.mocks.implementation

import com.shykial.kScraperCore.mocks.HttpCallMocker
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import it.skrape.fetcher.Request
import org.springframework.stereotype.Component

@Component
class SkrapeItCallCallMocker : HttpCallMocker {
    override fun mockHttpRequestCallUrl(originalUrl: String, mockedUrl: String) {
        mockkConstructor(Request::class)
        every { anyConstructed<Request>().url } returns mockedUrl
    }

    override fun clearMock() {
        unmockkConstructor(Request::class)
    }
}
