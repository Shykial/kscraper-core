package com.shykial.kScraperCore.exception

import org.springframework.http.HttpStatus

class AuthorizationException(
    message: String,
    cause: Throwable? = null
) : BaseAppException(message, cause, HttpStatus.FORBIDDEN)
