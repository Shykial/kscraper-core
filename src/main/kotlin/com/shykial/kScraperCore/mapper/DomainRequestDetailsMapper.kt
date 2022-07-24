package com.shykial.kScraperCore.mapper

import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import generated.com.shykial.kScraperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScraperCore.models.DomainRequestDetailsResponse

fun DomainRequestDetailsRequest.toEntity() = DomainRequestDetails(
    domainName = domainName,
    requestHeaders = requestHeaders,
    requestTimeoutInMillis = requestTimeoutInMillis
)

fun DomainRequestDetails.toResponse() = DomainRequestDetailsResponse(
    id = id,
    domainName = domainName,
    requestHeaders = requestHeaders,
    requestTimeoutInMillis = requestTimeoutInMillis
)

fun DomainRequestDetails.updateWith(request: DomainRequestDetailsRequest) = apply {
    domainName = request.domainName
    requestHeaders = request.requestHeaders
    requestTimeoutInMillis = request.requestTimeoutInMillis
}
