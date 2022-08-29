package com.shykial.kScraperCore.controller

import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.helper.AllowedForAdmin
import com.shykial.kScraperCore.helper.RestScope
import com.shykial.kScraperCore.service.AdminOperationsService
import generated.com.shykial.kScraperCore.apis.AdminOperationsApi
import generated.com.shykial.kScraperCore.models.ChangeAccountStateRequest
import generated.com.shykial.kScraperCore.models.ChangeUserEmailRequest
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
@AllowedForAdmin
class AdminOperationsController(
    private val adminOperationsService: AdminOperationsService
) : AdminOperationsApi, RestScope {
    private val log = KotlinLogging.logger { }

    override suspend fun changeAccountState(
        changeAccountStateRequest: ChangeAccountStateRequest
    ): ResponseEntity<Unit> = changeAccountStateRequest
        .also { log.info("Received request to change account's ${it.accountLogin} state to ${it.newState}") }
        .runSuspend(adminOperationsService::changeAccountState)
        .noContentResponseEntity()

    override suspend fun changeUserEmail(
        changeUserEmailRequest: ChangeUserEmailRequest
    ): ResponseEntity<Unit> = changeUserEmailRequest
        .also { log.info("Received request to change user's ${it.login} email to ${it.newEmail}") }
        .runSuspend(adminOperationsService::changeUserEmail)
        .noContentResponseEntity()
}
