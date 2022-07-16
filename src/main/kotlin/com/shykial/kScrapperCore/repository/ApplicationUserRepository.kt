package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.ApplicationUser
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ApplicationUserRepository : ReactiveMongoRepository<ApplicationUser, String> {
    fun findByLogin(login: String): Mono<ApplicationUser>
}
