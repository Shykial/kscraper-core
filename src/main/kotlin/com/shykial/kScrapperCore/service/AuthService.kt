package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.exception.AuthenticationException
import com.shykial.kScrapperCore.helper.saveIn
import com.shykial.kScrapperCore.mapper.toDocument
import com.shykial.kScrapperCore.repository.ApplicationUserRepository
import com.shykial.kScrapperCore.security.component.JwtProvider
import com.shykial.kScrapperCore.security.component.JwtToken
import generated.com.shykial.kScrapperCore.models.LoginRequest
import generated.com.shykial.kScrapperCore.models.RegisterUserRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val applicationUserRepository: ApplicationUserRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder
) {
    suspend fun login(loginRequest: LoginRequest): JwtToken =
        applicationUserRepository.findByLogin(loginRequest.login)
            ?.takeIf { passwordEncoder.matches(loginRequest.password, it.passwordHash) && !it.isDisabled }
            ?.let {
                jwtProvider.createToken(
                    subject = loginRequest.login,
                    roles = setOf(it.role)
                )
            } ?: throw AuthenticationException("Authentication failed for user ${loginRequest.login}")

    suspend fun registerNewUser(
        registerUserRequest: RegisterUserRequest
    ): String = registerUserRequest
        .let { it.toDocument(passwordEncoder.encode(it.password)) }
        .saveIn(applicationUserRepository)
        .id
}
