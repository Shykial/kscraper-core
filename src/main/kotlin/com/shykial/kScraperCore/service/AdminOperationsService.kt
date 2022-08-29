package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.exception.NotFoundException
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.model.entity.ApplicationUser
import com.shykial.kScraperCore.repository.ApplicationUserRepository
import generated.com.shykial.kScraperCore.models.AccountState
import generated.com.shykial.kScraperCore.models.ChangeAccountStateRequest
import generated.com.shykial.kScraperCore.models.ChangeUserEmailRequest
import org.springframework.stereotype.Service

@Service
class AdminOperationsService(
    private val applicationUserRepository: ApplicationUserRepository
) {
    suspend fun changeAccountState(
        changeAccountStateRequest: ChangeAccountStateRequest
    ) = with(changeAccountStateRequest) {
        applicationUserRepository.findByLogin(accountLogin)
            ?.updateState(newState)
            ?.saveIn(applicationUserRepository)
            ?: throw NotFoundException("Account not found for login $accountLogin")
    }

    suspend fun changeUserEmail(
        changeUserEmailRequest: ChangeUserEmailRequest
    ) = with(changeUserEmailRequest) {
        applicationUserRepository.findByLogin(login)
            ?.apply { email = newEmail }
            ?.saveIn(applicationUserRepository)
            ?: throw NotFoundException("Account not found for login $login")
    }

    private fun ApplicationUser.updateState(newState: AccountState) = apply {
        enabled = when (newState) {
            AccountState.ENABLED -> true
            AccountState.DISABLED -> false
        }
    }
}
