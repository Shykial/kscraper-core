package com.shykial.kScrapperCore.model.entity

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DomainRequestDetails(
    @Indexed(unique = true)
    val domainName: String,
    val requestHeaders: Map<String, String>? = null,
    val requestTimeoutInMillis: Int? = null,
) : BaseDocument()
