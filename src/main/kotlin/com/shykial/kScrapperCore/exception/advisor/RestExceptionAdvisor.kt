package com.shykial.kScrapperCore.exception.advisor

import com.shykial.kScrapperCore.common.toResponseEntity
import com.shykial.kScrapperCore.exception.DuplicateDataException
import com.shykial.kScrapperCore.exception.NotFoundException
import generated.com.shykial.kScrapperCore.models.ErrorResponse
import generated.com.shykial.kScrapperCore.models.ErrorResponse.ErrorType
import mu.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionAdvisor {
    private val log = KotlinLogging.logger { }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException) = ErrorResponse(
        errorType = ErrorType.NOT_FOUND,
        errorMessage = ex.message
    ).toResponseEntity(ex.httpStatus)

    @ExceptionHandler
    fun handleDuplicateDataException(ex: DuplicateDataException) = ErrorResponse(
        errorType = ErrorType.DUPLICATE_DATA,
        errorMessage = ex.message
    ).toResponseEntity(ex.httpStatus)

    @ExceptionHandler
    fun handleDuplicateKeyException(ex: DuplicateKeyException) =
        DuplicateDataException(
            message = "Duplicate data persisting error",
            cause = ex
        ).run(::handleDuplicateDataException)
}
