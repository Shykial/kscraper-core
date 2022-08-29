package com.shykial.kScraperCore.controller

import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.helper.AllowedForAll
import com.shykial.kScraperCore.helper.RestScope
import com.shykial.kScraperCore.service.AuthService
import generated.com.shykial.kScraperCore.apis.AuthApi
import generated.com.shykial.kScraperCore.models.AuthToken
import generated.com.shykial.kScraperCore.models.IdResponse
import generated.com.shykial.kScraperCore.models.LoginRequest
import generated.com.shykial.kScraperCore.models.RegisterUserRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
@AllowedForAll
class AuthController(
    private val authService: AuthService
) : AuthApi, RestScope {
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
