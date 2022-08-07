package com.shykial.kScraperCore.exception.advisor

import com.shykial.kScraperCore.exception.AuthenticationException
import com.shykial.kScraperCore.exception.AuthorizationException
import com.shykial.kScraperCore.exception.BaseAppException
import com.shykial.kScraperCore.exception.DuplicateDataException
import com.shykial.kScraperCore.exception.NotFoundException
import com.shykial.kScraperCore.helper.RestScope
import generated.com.shykial.kScraperCore.models.ErrorResponse
import generated.com.shykial.kScraperCore.models.ErrorType
import generated.com.shykial.kScraperCore.models.InvalidInputErrorResponse
import generated.com.shykial.kScraperCore.models.RejectedField
import mu.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class RestExceptionAdvisor : RestScope {
    private val log = KotlinLogging.logger { }

    @ExceptionHandler
    fun handleBaseException(ex: BaseAppException) = ex.toErrorResponse(ErrorType.INTERNAL_SEVER_ERROR)

    @ExceptionHandler
    fun handleNotFoundException(ex: NotFoundException) = ex.toErrorResponse(ErrorType.NOT_FOUND)

    @ExceptionHandler
    fun handleDuplicateDataException(ex: DuplicateDataException) = ex.toErrorResponse(ErrorType.DUPLICATE_DATA)

    @ExceptionHandler
    fun handleDuplicateKeyException(ex: DuplicateKeyException) =
        DuplicateDataException(
            message = "Duplicate data persisting error",
            cause = ex
        ).run(::handleDuplicateDataException)

    @ExceptionHandler
    fun handleAuthorizationException(ex: AuthorizationException) = ex.toErrorResponse(ErrorType.AUTHENTICATION_FAILURE)

    @ExceptionHandler
    fun handleAuthorizationException(ex: AuthenticationException) = ex.toErrorResponse(ErrorType.AUTHORIZATION_FAILURE)

    @ExceptionHandler
    fun handleMethodArgumentNotValidException(ex: WebExchangeBindException) = ex
        .also { log.warn { "handling validation exception ${ex.message}" } }
        .run {
            InvalidInputErrorResponse(
                errorType = ErrorType.INVALID_INPUT,
                errorMessage = "Request payload validation failed",
                rejectedFields = fieldErrors.map { RejectedField(it.field, it.rejectedValue) }
            )
        }.toResponseEntity(HttpStatus.BAD_REQUEST)

    private fun BaseAppException.toErrorResponse(errorType: ErrorType): ResponseEntity<ErrorResponse> {
        log.warn("handling exception with message: $message")
        return ErrorResponse(
            errorType = errorType,
            errorMessage = message
        ).toResponseEntity(httpStatus)
    }
}
