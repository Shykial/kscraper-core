package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.model.entity.DomainRequestDetails
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsRequest
import generated.com.shykial.kScrapperCore.models.DomainRequestDetailsResponse

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
