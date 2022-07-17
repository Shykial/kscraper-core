package com.shykial.kScrapperCore.exception.advisor

import com.shykial.kScrapperCore.exception.AuthenticationException
import com.shykial.kScrapperCore.exception.AuthorizationException
import com.shykial.kScrapperCore.exception.BaseAppException
import com.shykial.kScrapperCore.exception.DuplicateDataException
import com.shykial.kScrapperCore.exception.NotFoundException
import com.shykial.kScrapperCore.helper.toResponseEntity
import generated.com.shykial.kScrapperCore.models.ErrorResponse
import generated.com.shykial.kScrapperCore.models.ErrorType
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionAdvisor {

    @ExceptionHandler
    fun handleBaseException(ex: BaseAppException) = ErrorResponse(
        errorType = ErrorType.INTERNAL_SEVER_ERROR,
        errorMessage = ex.message
    ).toResponseEntity(ex.httpStatus)

    @ExceptionHandler
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

    @ExceptionHandler
    fun handleAuthorizationException(ex: AuthorizationException) = ErrorResponse(
        errorType = ErrorType.AUTHORIZATION_FAILURE,
        errorMessage = ex.message
    ).toResponseEntity(ex.httpStatus)

    @ExceptionHandler
    fun handleAuthorizationException(ex: AuthenticationException) = ErrorResponse(
        errorType = ErrorType.AUTHENTICATION_FAILURE,
        errorMessage = ex.message
    ).toResponseEntity(ex.httpStatus)
}
