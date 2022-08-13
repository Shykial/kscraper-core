package com.shykial.kScraperCore.service

import com.ninjasquad.springmockk.MockkBean
import com.shykial.kScraperCore.config.KScraperAuditor
import com.shykial.kScraperCore.helper.WithPreInitializedUsers
import com.shykial.kScraperCore.helper.findRefreshed
import com.shykial.kScraperCore.helper.saveIn
import com.shykial.kScraperCore.init.UsersInitializer
import com.shykial.kScraperCore.model.entity.BaseDocument
import com.shykial.kScraperCore.model.entity.DomainRequestDetails
import com.shykial.kScraperCore.model.entity.ExtractedProperty
import com.shykial.kScraperCore.model.entity.ExtractingDetails
import com.shykial.kScraperCore.model.entity.Selector
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.repository.DomainRequestDetailsRepository
import com.shykial.kScraperCore.repository.ExtractingDetailsRepository
import com.shykial.kScraperCore.starter.MongoDBStarter
import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainInOrder
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import java.net.SocketTimeoutException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
@ActiveProfiles("test")
class ScrapingFailureDetectionTest(
    private val scrapingFailureDetectionService: ScrapingFailureDetectionService,
    private val domainRequestDetailsRepository: DomainRequestDetailsRepository,
    private val extractingDetailsRepository: ExtractingDetailsRepository,
    override val usersInitializer: UsersInitializer,
    @Value("\${scraping.max-attempts}") private val scrapingMaxAttempts: Int
) : MongoDBStarter, WithPreInitializedUsers {

    @MockkBean
    private lateinit var javaMailSender: JavaMailSender

    val adminEmails = usersInitializer.groupedInitUsers[UserRole.ADMIN]!!.map { it.email }

    @BeforeEach
    fun setup() = runTest {
        clearAllMocks()
        launch { domainRequestDetailsRepository.deleteAll() }
        launch { extractingDetailsRepository.deleteAll() }
        every { javaMailSender.createMimeMessage() } returns MimeMessage(mockk<Session>(relaxed = true))
    }

    @Test
    fun `should detect domain request details failure and notify owner and admins via email`() = runTest {
        // given
        val mockedCreatorEmail = "mockedDomainRequestDetailsEmail@email.com"
        val initialScrapingFailures = scrapingMaxAttempts - 1
        val initialDomainRequestDetails = createSampleDomainRequestDetails(initialScrapingFailures)
            .saveIn(domainRequestDetailsRepository)
            .toSpyWithMockedCreatorMail(mockedCreatorEmail)
        val capturedMessageSlot = slot<MimeMessage>()
        justRun { javaMailSender.send(capture(capturedMessageSlot)) }

        // when
        runCatching {
            scrapingFailureDetectionService.runDetectingScrapingFailures(initialDomainRequestDetails) {
                throw SocketTimeoutException("sample socket timeout exception")
            }
        }

        // then
        domainRequestDetailsRepository.findRefreshed(initialDomainRequestDetails)
            .currentScrapeFailures shouldBe initialScrapingFailures + 1

        eventually(5.seconds) {
            capturedMessageSlot.captured.run {
                subject.shouldContainInOrder("Domain Request Details", initialDomainRequestDetails.domainName)

                allRecipients
                    .map { (it as InternetAddress).address }
                    .shouldContainExactlyInAnyOrder(adminEmails + initialDomainRequestDetails.createdBy!!.email)

                (content as String) shouldContain "failure number [${initialScrapingFailures + 1}]"
            }
        }
    }

    @Test
    fun `should detect extracting details scraping failure and notify owner and admins via email`() = runTest {
        // given
        val mockedCreatorEmail = "mockedExtractingDetails@email.com"
        val initialScrapingFailures = scrapingMaxAttempts - 1

        val usedExtractingDetails = createSampleExtractingDetails(initialScrapingFailures)
            .saveIn(extractingDetailsRepository)
            .toSpyWithMockedCreatorMail(mockedCreatorEmail)

        val capturedMessageSlot = slot<MimeMessage>()
        justRun { javaMailSender.send(capture(capturedMessageSlot)) }

        // when
        scrapingFailureDetectionService.runDetectingScrapingFailures(mockk()) {
            mockk(relaxed = true) {
                every { failedDetails } returns listOf(usedExtractingDetails)
            }
        }

        // then
        extractingDetailsRepository.findRefreshed(usedExtractingDetails)
            .currentScrapeFailures shouldBe initialScrapingFailures + 1

        eventually(5.seconds) {
            capturedMessageSlot.captured.run {
                subject.shouldContainInOrder("Extracting Details", usedExtractingDetails.domainId)

                allRecipients
                    .map { (it as InternetAddress).address }
                    .shouldContainExactlyInAnyOrder(adminEmails + usedExtractingDetails.createdBy!!.email) //

                (content as String) shouldContain "failure number [${initialScrapingFailures + 1}]"
            }
        }
    }

    @Test
    fun `should not send email when scraping fails but failure counter is below threshold`() = runTest {
        val mockedCreatorEmail = "mockedExtractingDetails@email.com"
        val initialScrapingFailures = scrapingMaxAttempts - 2

        val usedExtractingDetails = createSampleExtractingDetails(initialScrapingFailures)
            .saveIn(extractingDetailsRepository)
            .toSpyWithMockedCreatorMail(mockedCreatorEmail)

        // when
        scrapingFailureDetectionService.runDetectingScrapingFailures(mockk()) {
            mockk(relaxed = true) {
                every { failedDetails } returns listOf(usedExtractingDetails)
            }
        }

        // then
        extractingDetailsRepository.findRefreshed(usedExtractingDetails)
            .currentScrapeFailures shouldBe initialScrapingFailures + 1

        continually(3.seconds) {
            verify { javaMailSender wasNot Called }
        }
    }

    private fun createSampleDomainRequestDetails(
        initialScrapingFailures: Int
    ) = DomainRequestDetails(
        domainName = "sampleDomainName",
        requestHeaders = mapOf(),
        requestTimeoutInMillis = null
    ).apply { currentScrapeFailures = initialScrapingFailures }

    private fun createSampleExtractingDetails(
        initialScrapingFailures: Int
    ) = ExtractingDetails(
        domainId = "sampleDomainId",
        fieldName = "sampleFieldName",
        selector = Selector(value = "", index = 0),
        extractedProperty = ExtractedProperty.OwnText
    ).apply { currentScrapeFailures = initialScrapingFailures }

    private inline fun <reified T : BaseDocument> T.toSpyWithMockedCreatorMail(
        mockedAuditorMail: String
    ) = spyk(this) {
        every { createdBy } returns KScraperAuditor(
            login = "login",
            email = mockedAuditorMail,
            roles = setOf(UserRole.DEV)
        )
    }
}
