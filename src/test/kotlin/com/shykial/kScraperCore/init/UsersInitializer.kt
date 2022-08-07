package com.shykial.kScraperCore.init

import com.shykial.kScraperCore.helper.saveAllIn
import com.shykial.kScraperCore.model.entity.ApplicationUser
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.repository.ApplicationUserRepository
import com.shykial.kScraperCore.security.component.JwtProvider
import com.shykial.kScraperCore.security.component.JwtToken
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class UsersInitializer(
    private val applicationUserRepository: ApplicationUserRepository,
    private val jwtProvider: JwtProvider
) {
    private val log = KotlinLogging.logger { }
    private val users = listOf(
        "admin1" to UserRole.ADMIN,
        "dev1" to UserRole.DEV,
        "dev2" to UserRole.DEV,
        "apiUser1" to UserRole.API_USER
    ).map { (login, role) ->
        ApplicationUser(
            login = login,
            passwordHash = "",
            email = "$login@$role.com",
            role = role
        )
    }
    private val groupedUsers = users.groupBy { it.role }

    suspend fun assureUsersPresentInDB() = users
        .also { log.info { "Persisting users $it" } }
        .saveAllIn(applicationUserRepository)

    fun getAdminJwtToken(adminLogin: String? = null): JwtToken = groupedUsers[UserRole.ADMIN]
        ?.let { admins ->
            adminLogin?.let { login -> admins.single { it.login == login } } ?: admins[0]
        }?.run {
            jwtProvider.createToken(
                subject = login,
                roles = listOf(role)
            )
        } ?: error("Admin user not initialized properly")
}