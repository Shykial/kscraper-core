package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.extension.runSuspend
import com.shykial.kScrapperCore.helper.toResponseEntity
import com.shykial.kScrapperCore.security.service.JwtLoginService
import generated.com.shykial.kScrapperCore.apis.AuthApi
import generated.com.shykial.kScrapperCore.models.AuthToken
import generated.com.shykial.kScrapperCore.models.LoginRequest
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val jwtLoginService: JwtLoginService
) : AuthApi {
    private val log = KotlinLogging.logger { }

    override suspend fun login(
        loginRequest: LoginRequest
    ): ResponseEntity<AuthToken> = loginRequest
        .also { log.info("Received login request for user ${it.login}") }
        .runSuspend(jwtLoginService::login)
        .run(::AuthToken)
        .toResponseEntity()

}
