package com.shykial.kScraperCore.exception

import org.springframework.http.HttpStatus

class NotFoundException(
    message: String,
    cause: Throwable? = null
) : BaseAppException(message, cause, HttpStatus.NOT_FOUND)
