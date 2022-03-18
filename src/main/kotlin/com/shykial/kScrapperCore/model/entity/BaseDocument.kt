package com.shykial.kScrapperCore.model.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

abstract class BaseDocument {
    @Id
    private var _id: String = ObjectId.get().toHexString()

    val id
        get() = _id
}
