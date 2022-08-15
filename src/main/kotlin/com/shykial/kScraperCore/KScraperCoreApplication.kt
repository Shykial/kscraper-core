package com.shykial.kScraperCore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableAsync
class KScraperCoreApplication

fun main(args: Array<String>) {
    runApplication<KScraperCoreApplication>(*args)
}
