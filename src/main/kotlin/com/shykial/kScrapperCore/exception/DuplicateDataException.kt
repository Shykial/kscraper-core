package com.shykial.kScrapperCore.exception

import org.springframework.http.HttpStatus

class DuplicateDataException(
    message: String,
    cause: Throwable?,
) : BaseAppException(message, cause, HttpStatus.CONFLICT)
