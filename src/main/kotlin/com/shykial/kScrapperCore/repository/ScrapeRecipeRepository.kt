package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.ScrapeRecipe
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ScrapeRecipeRepository : ReactiveMongoRepository<ScrapeRecipe, String> {
    fun findByDomainName(domainName: String): Mono<ScrapeRecipe>
}