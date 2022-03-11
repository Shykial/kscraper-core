package com.shykial.kScrapperCore.model.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DomainRequestDetails(
    @Indexed(unique = true)
    val domainName: String,
    val requestHeaders: Map<String, String>? = null,
    val requestTimeoutInMillis: Int? = null,
) {
    @Id
    private var _id: String = ObjectId.get().toHexString()

    val id
        get() = _id
}