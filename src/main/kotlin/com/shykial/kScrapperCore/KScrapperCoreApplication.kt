package com.shykial.kScrapperCore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories
class KScrapperCoreApplication

fun main(args: Array<String>) {
    runApplication<KScrapperCoreApplication>(*args)
}
