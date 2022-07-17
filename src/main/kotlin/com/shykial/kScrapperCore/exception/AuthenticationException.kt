package com.shykial.kScrapperCore.exception

import org.springframework.http.HttpStatus

class AuthenticationException(
    message: String,
    cause: Throwable? = null
) : BaseAppException(message, cause, HttpStatus.UNAUTHORIZED)
