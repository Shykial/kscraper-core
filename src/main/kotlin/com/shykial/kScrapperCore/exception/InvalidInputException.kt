package com.shykial.kScrapperCore.exception

import org.springframework.http.HttpStatus

class InvalidInputException(
    message: String,
    cause: Throwable? = null
) : BaseAppException(message, cause, HttpStatus.BAD_REQUEST)
