package com.shykial.kScrapperCore.controller

import generated.com.shykial.kScrapperCore.apis.AuthApi
import generated.com.shykial.kScrapperCore.models.AuthToken
import generated.com.shykial.kScrapperCore.models.LoginRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController : AuthApi {
    override suspend fun login(loginRequest: LoginRequest): ResponseEntity<AuthToken> {
        return super.login(loginRequest)
    }
}
