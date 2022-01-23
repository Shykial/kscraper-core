package com.shykial.kScrapperCore.model.dto

class DomainRequestDetailsRequest(
    val domainName: String,
    val requestHeaders: Map<String, String>? = null,
    val requestTimeoutInMillis: Int? = null,
)