package com.shykial.kScraperCore.model.entity

import com.shykial.kScraperCore.configuration.KScraperAuditor
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.time.Instant

abstract class BaseDocument {
    @Id
    private var _id: String = ObjectId.get().toHexString()
    val id get() = _id

    @CreatedBy
    private var _createdBy: KScraperAuditor? = null
    val createdBy get() = _createdBy

    @CreatedDate
    private var _createdAt: Instant? = null
    val createdAt get() = _createdAt

    @Version
    private var _version: Int? = null
    val version get() = _version

    @LastModifiedDate
    private var _updatedAt: Instant? = null
    val updatedAt get() = _updatedAt

    @LastModifiedBy
    private var _modifiedBy: KScraperAuditor? = null
    val modifiedBy get() = _modifiedBy
}
