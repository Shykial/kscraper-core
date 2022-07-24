package com.shykial.kScraperCore.exception

import org.springframework.http.HttpStatus

abstract class BaseAppException(
    override val message: String,
    override val cause: Throwable?,
    val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
) : RuntimeException(message, cause)
