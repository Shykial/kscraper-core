package com.shykial.kScraperCore.config

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfiguration {

    @Bean
    fun objectMapper() = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        setSerializationInclusion(Include.NON_NULL)
    }
}
