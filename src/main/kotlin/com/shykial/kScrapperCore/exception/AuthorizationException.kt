package com.shykial.kScrapperCore.exception

import org.springframework.http.HttpStatus

class AuthorizationException(
    message: String,
    cause: Throwable?
) : BaseAppException(message, cause, HttpStatus.UNAUTHORIZED) // todo handle mapping differently
