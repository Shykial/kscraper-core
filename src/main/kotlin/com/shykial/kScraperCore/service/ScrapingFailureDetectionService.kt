package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.extension.alsoSuspend
import com.shykial.kScraperCore.helper.saveAllIn
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.helper.withRetries
import com.shykial.kScraperCore.model.ScrapedData
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.repository.ApplicationUserRepository
import com.shykial.kScraperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ScrapingFailureDetectionService(
    private val applicationUserRepository: ApplicationUserRepository,
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
    private val emailService: EmailService,
    @Value("\${scraping.max-attempts}") private val scrapingMaxAttempts: Int
) {
    suspend fun runDetectingScrapingFailures(
        domainRequestDetails: DomainRequestDetails,
        block: suspend (Int) -> ScrapedData
    ) = withRetries(
        maxAttempts = scrapingMaxAttempts,
        block = { block(it).alsoSuspend(::validateAndHandleScrapedDataResult) },
        failureCallback = { handleScrapingFailure(domainRequestDetails) }
    )

    private suspend fun handleScrapingFailure(domainRequestDetails: DomainRequestDetails) {
        domainRequestDetails
            .apply {
                currentScrapeFailures++
                notifyIfNeeded()
            }.saveIn(domainRequestDetailsRepository)
    }

    private suspend fun validateAndHandleScrapedDataResult(scrapedData: ScrapedData) {
        buildList {
            scrapedData.failedDetails
                .onEach {
                    it.apply {
                        currentScrapeFailures++
                        notifyIfNeeded()
                    }
                }.run(::addAll)
            scrapedData.scrapedFields.keys
                .onEach { it.apply { currentScrapeFailures = 0 } }
                .run(::addAll)
        }.saveAllIn(extractingDetailsRepository)
    }

    private suspend fun ExtractingDetails.notifyIfNeeded() {
        if ((currentScrapeFailures >= 0) && (currentScrapeFailures % scrapingMaxAttempts == 0)) {
            buildList {
                addAll(applicationUserRepository.findByRole(UserRole.ADMIN).map { it.email })
                createdBy?.email?.let { add(it) }
            }.forEach { sendExtractingDetailsFailureEmail(this, it) }
        }
    }

    private suspend fun DomainRequestDetails.notifyIfNeeded() {
        if ((currentScrapeFailures >= 0) && (currentScrapeFailures % scrapingMaxAttempts == 0)) {
            buildList {
                addAll(applicationUserRepository.findByRole(UserRole.ADMIN).map { it.email })
                createdBy?.email?.let { add(it) }
            }.forEach { sendDomainRequestDetailsFailureEmail(this, it) }
        }
    }

    private suspend fun sendExtractingDetailsFailureEmail(
        extractingDetails: ExtractingDetails,
        toEmail: String
    ) {
        emailService.sendMail(
            toEmail = toEmail,
            subject = "Scraping failure for extracting Details for domain with ID[${extractingDetails.domainId}]",
            content = """<h3>Scraping failed for extractingDetails:</h3>
                         |<p><i>$extractingDetails</i></p>
                         |<h5>failure number [${extractingDetails.currentScrapeFailures}]</h5>
            """.trimMargin(),
            isHtmlContent = true
        )
    }

    private suspend fun sendDomainRequestDetailsFailureEmail(
        domainRequestDetails: DomainRequestDetails,
        toEmail: String
    ) {
        emailService.sendMail(
            toEmail = toEmail,
            subject = "Scraping failure for domain request details for domain [${domainRequestDetails.domainName}]",
            content = """<h3>Scraping failed for domainRequestDetails:</h3>
                         |<p><i>$domainRequestDetails</i></p>
                         |<h5>failure number [${domainRequestDetails.currentScrapeFailures}]</h5>
            """.trimMargin(),
            isHtmlContent = true
        )
    }
}
