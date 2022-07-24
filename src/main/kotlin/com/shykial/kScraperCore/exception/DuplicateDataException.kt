package com.shykial.kScraperCore.exception

import org.springframework.http.HttpStatus

class DuplicateDataException(
    message: String,
    cause: Throwable?,
) : BaseAppException(message, cause, HttpStatus.CONFLICT)
