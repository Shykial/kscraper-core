package com.shykial.kScraperCore.model.entity

import com.shykial.kScraperCore.configuration.KScraperAuditor
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

abstract class BaseDocument {
    @Id
    private var _id: String = ObjectId.get().toHexString()
    val id get() = _id

    @Version
    @Field(name = "version")
    private var _version: Int? = null
    val version get() = _version

    @CreatedBy
    @Field(name = "createdBy")
    private var _createdBy: KScraperAuditor? = null
    val createdBy get() = _createdBy

    @CreatedDate
    @Field(name = "createdAt")
    private var _createdAt: Instant? = null
    val createdAt get() = _createdAt

    @LastModifiedDate
    @Field(name = "updatedAt")
    private var _updatedAt: Instant? = null
    val updatedAt get() = _updatedAt

    @LastModifiedBy
    @Field(name = "modifiedBy")
    private var _modifiedBy: KScraperAuditor? = null
    val modifiedBy get() = _modifiedBy
}
