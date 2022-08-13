package com.shykial.kScraperCore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableSwagger2
@EnableAsync
class KScraperCoreApplication

fun main(args: Array<String>) {
    runApplication<KScraperCoreApplication>(*args)
}
