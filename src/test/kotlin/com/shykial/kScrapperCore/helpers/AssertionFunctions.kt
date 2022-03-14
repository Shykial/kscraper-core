package com.shykial.kScrapperCore.helpers

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.assertj.core.api.Assertions.assertThat
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

suspend fun <T> Mono<T>.awaitAndAssertNull() = assertThat(awaitSingleOrNull()).isNull()

suspend fun <T> Flux<T>.awaitAndAssertEmpty() = assertThat(collectList().awaitSingle()).isEmpty()
