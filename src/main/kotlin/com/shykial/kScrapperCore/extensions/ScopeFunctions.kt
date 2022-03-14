package com.shykial.kScrapperCore.extensions

suspend inline fun <T, R> T.runSuspend(crossinline block: suspend T.() -> R) = block()
