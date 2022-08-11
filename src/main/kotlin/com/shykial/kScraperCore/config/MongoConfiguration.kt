package com.shykial.kScraperCore.config

import com.shykial.kScraperCore.extension.runSuspend
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.repository.ApplicationUserRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.bson.BsonRegularExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.regex.Pattern

@EnableReactiveMongoAuditing
@Configuration
class MongoConfiguration {
    @Bean
    fun conversions(): MongoCustomConversions = MongoCustomConversions(listOf(RegexReadConverter))
}

@Component
class KScraperAuditorProvider(
    private val applicationUserRepository: ApplicationUserRepository
) : ReactiveAuditorAware<KScraperAuditor> {
    override fun getCurrentAuditor(): Mono<KScraperAuditor> = mono {
        ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()
            ?.authentication?.name
            ?.runSuspend(applicationUserRepository::findByLogin)
            ?.let { user ->
                KScraperAuditor(
                    login = user.login,
                    email = user.email,
                    roles = setOf(user.role)
                )
            } ?: KScraperAuditor.SYSTEM
    }
}

@ReadingConverter
object RegexReadConverter : Converter<BsonRegularExpression, Pattern> {
    override fun convert(source: BsonRegularExpression): Pattern = Pattern.compile(source.pattern)
}

data class KScraperAuditor(
    val login: String,
    val email: String,
    val roles: Set<UserRole>
) {
    companion object {
        val SYSTEM = KScraperAuditor(
            login = "SYSTEM",
            email = "",
            roles = setOf(UserRole.SYSTEM)
        )
    }
}
