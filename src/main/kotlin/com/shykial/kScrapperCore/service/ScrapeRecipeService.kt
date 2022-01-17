package com.shykial.kScrapperCore.service

import com.shykial.kScrapperCore.repository.ScrapeRecipeRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ScrapeRecipeService(private val scrapeRecipeRepository: ScrapeRecipeRepository) {
    private val log = KotlinLogging.logger { }
}
