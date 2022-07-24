package com.shykial.kScraperCore.repository

import com.shykial.kScraperCore.model.entity.ApplicationUser
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationUserRepository : CoroutineCrudRepository<ApplicationUser, String> {
    suspend fun findByLogin(login: String): ApplicationUser?
}
