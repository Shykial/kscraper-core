package com.shykial.kScraperCore.mocks

interface HttpCallMocker {
    fun mockHttpRequestCallUrl(originalUrl: String, mockedUrl: String)

    fun clearMock()
}
