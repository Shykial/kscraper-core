package com.shykial.kScrapperCore.security.exception

import org.springframework.security.core.AuthenticationException

class JwtAuthenticationException(
    message: String?,
    cause: Throwable?
) : AuthenticationException(message, cause)
