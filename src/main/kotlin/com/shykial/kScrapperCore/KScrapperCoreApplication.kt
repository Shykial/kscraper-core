package com.shykial.kScrapperCore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableSwagger2
class KScrapperCoreApplication

fun main(args: Array<String>) {
    runApplication<KScrapperCoreApplication>(*args)
}
