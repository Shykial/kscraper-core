package com.shykial.kScraperCore.helper

import com.shykial.kScraperCore.model.entity.BaseDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

suspend fun <T : BaseDocument> CoroutineCrudRepository<T, String>.findRefreshed(
    document: T
) = findById(document.id)!!
