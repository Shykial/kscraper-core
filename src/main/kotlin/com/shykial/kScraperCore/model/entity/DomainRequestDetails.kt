package com.shykial.kScraperCore.model.entity

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DomainRequestDetails(
    @Indexed(unique = true)
    var domainName: String,
    var requestHeaders: Map<String, String>? = null,
    var requestTimeoutInMillis: Int? = null,
) : BaseDocument()
