package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest

fun DomainRequestDetailsRequest.toEntity() = DomainRequestDetails(
    domainName = domainName,
    requestHeaders = requestHeaders,
    requestTimeoutInMillis = requestTimeoutInMillis
)