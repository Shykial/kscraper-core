package com.shykial.kScraperCore.service

import com.shykial.kScraperCore.helper.withRetries
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID
import kotlin.system.measureTimeMillis

@Service
class EmailService(
    @Value("\${spring.mail.username}") private val fromEmail: String,
    private val javaMailSender: JavaMailSender
) {
    private val log = KotlinLogging.logger { }

    @Async
    suspend fun sendMail(
        toEmail: String,
        subject: String,
        content: String,
        isHtmlContent: Boolean
    ) {
        withRetries(maxAttempts = 5, delay = Duration.ofSeconds(2)) { attempt ->
            val requestId = UUID.randomUUID()
            measureTimeMillis {
                log.info("[$requestId] Sending message with subject [$subject] to email [$toEmail], attempt [$attempt]")
                javaMailSender.createMimeMessage().also {
                    MimeMessageHelper(it).apply {
                        setFrom(fromEmail)
                        setTo(toEmail)
                        setSubject(subject)
                        setText(content, isHtmlContent)
                    }
                }.run(javaMailSender::send)
            }.also { log.info("[$requestId] Email Sent after $it milliseconds") }
        }
    }
}
