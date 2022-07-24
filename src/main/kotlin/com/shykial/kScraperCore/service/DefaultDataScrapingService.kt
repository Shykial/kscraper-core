package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.staticImplementation.SkrapeItDataScraper
import com.shykial.kScraperCore.useCase.ScrapeForDataUseCase
import org.springframework.stereotype.Service

@Service
class DefaultDataScrapingService : ScrapeForDataUseCase by SkrapeItDataScraper