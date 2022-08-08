package com.shykial.kScraperCore.config

import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.security.ROLE_PREFIX
import org.bson.BsonRegularExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import java.util.regex.Pattern

@EnableReactiveMongoAuditing
@Configuration
class MongoConfiguration {

    @Bean
    fun auditorProvider(): ReactiveAuditorAware<KScraperAuditor> = ReactiveAuditorAware<KScraperAuditor> {
        ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication }
            .map { auth ->
                KScraperAuditor(
                    login = auth.name,
                    roles = auth.authorities.map { UserRole.valueOf(it.authority.removePrefix(ROLE_PREFIX)) }.toSet()
                )
            }.defaultIfEmpty(KScraperAuditor("SYSTEM", setOf(UserRole.SYSTEM)))
    }

    @Bean
    fun conversions(): MongoCustomConversions = MongoCustomConversions(listOf(RegexReadConverter))
}

@ReadingConverter
object RegexReadConverter : Converter<BsonRegularExpression, Pattern> {
    override fun convert(source: BsonRegularExpression): Pattern = Pattern.compile(source.pattern)
}

data class KScraperAuditor(
    val login: String,
    val roles: Set<UserRole>
)
