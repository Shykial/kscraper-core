package com.shykial.kScraperCore.helper

import com.shykial.kScraperCore.model.entity.BaseDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

fun Instant.plusMinutes(numberOfMinutes: Long): Instant = plusSeconds(numberOfMinutes * 60)

suspend fun <T : BaseDocument> CoroutineCrudRepository<T, String>.findRefreshed(
    document: T
) = findById(document.id)!!
