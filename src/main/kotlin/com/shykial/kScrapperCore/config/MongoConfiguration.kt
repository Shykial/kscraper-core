package com.shykial.kScrapperCore.config

import org.bson.BsonRegularExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.util.regex.Pattern

@Configuration
class MongoConfiguration {
    @Bean
    fun conversions(): MongoCustomConversions = MongoCustomConversions(listOf(RegexReadConverter))
}

@ReadingConverter
object RegexReadConverter : Converter<BsonRegularExpression, Pattern> {
    override fun convert(source: BsonRegularExpression): Pattern = Pattern.compile(source.pattern)
}