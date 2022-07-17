package com.shykial.kScrapperCore.helper

import com.shykial.kScrapperCore.model.entity.BaseDocument
import kotlinx.coroutines.flow.toList
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> T.toResponseEntity(status: HttpStatus = HttpStatus.OK): ResponseEntity<T> = ResponseEntity(this, status)

suspend fun <T : BaseDocument> T.saveIn(
    repository: CoroutineCrudRepository<T, *>
): T = repository.save(this)

suspend fun <T : BaseDocument> Iterable<T>.saveAllIn(
    repository: CoroutineCrudRepository<T, *>
) = repository.saveAll(this).toList()
