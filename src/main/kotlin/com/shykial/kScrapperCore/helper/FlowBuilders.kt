package com.shykial.kScrapperCore.helper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

inline fun <T> iterableFlow(crossinline block: suspend () -> Iterable<T>): Flow<T> = flow {
    block().forEach { emit(it) }
}