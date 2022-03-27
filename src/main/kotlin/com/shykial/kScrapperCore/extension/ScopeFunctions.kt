package com.shykial.kScrapperCore.extension

suspend inline fun <T, R> T.runSuspend(crossinline block: suspend T.() -> R): R = block()
