package com.shykial.kScrapperCore.security.service

import com.shykial.kScrapperCore.exception.AuthenticationException
import com.shykial.kScrapperCore.repository.ApplicationUserRepository
import com.shykial.kScrapperCore.security.component.JwtProvider
import generated.com.shykial.kScrapperCore.models.LoginRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class JwtLoginService(
    private val applicationUserRepository: ApplicationUserRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder
) {
    suspend fun login(loginRequest: LoginRequest): String =
        applicationUserRepository.findByLogin(loginRequest.login)
            ?.takeIf { passwordEncoder.matches(loginRequest.password, it.passwordHash) && !it.isDisabled }
            ?.let {
                jwtProvider.createToken(
                    subject = loginRequest.login,
                    roles = setOf(it.role)
                )
            } ?: throw AuthenticationException("Authentication failed for user ${loginRequest.login}")
}