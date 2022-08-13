package com.shykial.kScraperCore.repository

import com.shykial.kScraperCore.model.entity.ApplicationUser
import com.shykial.kScraperCore.model.entity.UserRole
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationUserRepository : CoroutineCrudRepository<ApplicationUser, String> {
    suspend fun findByLogin(login: String): ApplicationUser?

    suspend fun findByRole(role: UserRole): List<ApplicationUser>
}
