package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.model.dto.DomainRequestDetailsRequest
import com.shykial.kScrapperCore.model.entity.DomainRequestDetails

fun DomainRequestDetailsRequest.toEntity() = DomainRequestDetails(
    domainName = domainName,
    requestHeaders = requestHeaders,
    requestTimeoutInMillis = requestTimeoutInMillis
)