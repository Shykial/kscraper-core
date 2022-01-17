package com.shykial.kScrapperCore.repository

import com.shykial.kScrapperCore.model.entity.ScrapingRecipe
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ScrapeRecipeRepository : ReactiveMongoRepository<ScrapingRecipe, String> {
    fun findByDomainName(domainName: String): Mono<ScrapingRecipe>
}
