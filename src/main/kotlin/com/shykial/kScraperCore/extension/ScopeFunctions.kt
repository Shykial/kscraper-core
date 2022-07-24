package com.shykial.kScraperCore.extension

suspend inline fun <T, R> T.runSuspend(crossinline block: suspend T.() -> R): R = block()

suspend inline fun <T> T.alsoSuspend(crossinline block: suspend (T) -> Unit) = also { block(this) }
