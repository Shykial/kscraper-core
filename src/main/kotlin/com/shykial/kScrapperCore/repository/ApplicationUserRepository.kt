package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.ApplicationUser
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationUserRepository : CoroutineCrudRepository<ApplicationUser, String> {
    suspend fun findByLogin(login: String): ApplicationUser?
}
