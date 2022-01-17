package com.shykial.kScrapperCore.controller

import com.shykial.kScrapperCore.model.dto.ScrapeRecipeRequest
import com.shykial.kScrapperCore.service.ScrapeRecipeService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("scraping-recipe")
class ScrapingRecipeController(
    private val scrapeRecipeService: ScrapeRecipeService
) {
    private val log = KotlinLogging.logger { }

    @PostMapping
    suspend fun addNewRecipe(@RequestBody scrapeRecipeRequest: ScrapeRecipeRequest) {
        log.info("Received request for adding new recipe for domain ${scrapeRecipeRequest.domainName}")
        TODO()
    }
}
