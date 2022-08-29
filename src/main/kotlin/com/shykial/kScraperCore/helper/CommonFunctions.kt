package com.shykial.kScraperCore.helper

import com.shykial.kScraperCore.model.entity.BaseDocument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.time.Duration

interface RestScope {
    fun <T> T.toResponseEntity(status: HttpStatus = HttpStatus.OK): ResponseEntity<T> = ResponseEntity(this, status)

    fun Any?.noContentResponseEntity() = ResponseEntity<Unit>(HttpStatus.NO_CONTENT)
}

suspend fun <T : BaseDocument> T.saveIn(
    repository: CoroutineCrudRepository<T, *>
): T = repository.save(this)

suspend fun <T : BaseDocument> Iterable<T>.saveAllIn(
    repository: CoroutineCrudRepository<T, *>
) = repository.saveAll(this).toList()

suspend inline fun <T> withRetries(
    maxAttempts: Int,
    delay: Duration = Duration.ZERO,
    failureCallback: (Throwable) -> Unit = { },
    block: (Int) -> T
): T {
    repeat(maxAttempts - 1) { attempt ->
        runCatching { block(attempt) }.onSuccess { return it }
        delay(delay.inWholeMilliseconds)
    }
    return runCatching { block(maxAttempts) }.onFailure(failureCallback).getOrThrow()
}
