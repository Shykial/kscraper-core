package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.extension.runSuspend
import com.shykial.kScrapperCore.helper.toResponseEntity
import com.shykial.kScrapperCore.service.AuthService
import generated.com.shykial.kScrapperCore.apis.AuthApi
import generated.com.shykial.kScrapperCore.models.AuthToken
import generated.com.shykial.kScrapperCore.models.IdResponse
import generated.com.shykial.kScrapperCore.models.LoginRequest
import generated.com.shykial.kScrapperCore.models.RegisterUserRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) : AuthApi {
    private val log = KotlinLogging.logger { }

    override suspend fun login(
        loginRequest: LoginRequest
    ): ResponseEntity<AuthToken> = loginRequest
        .also { log.info("Received login request for user ${it.login}") }
        .runSuspend(authService::login)
        .run { AuthToken(token) }
        .toResponseEntity()

    override suspend fun register(
        registerUserRequest: RegisterUserRequest
    ): ResponseEntity<IdResponse> = registerUserRequest
        .also { log.info("Received registration request with login ${it.login}") }
        .runSuspend(authService::registerNewUser)
        .run(::IdResponse)
        .toResponseEntity(HttpStatus.CREATED)
}
