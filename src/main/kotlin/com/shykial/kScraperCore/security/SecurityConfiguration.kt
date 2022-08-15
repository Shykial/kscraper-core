package com.shykial.kScraperCore.security

import com.shykial.kScraperCore.security.service.JwtAuthenticationManager
import com.shykial.kScraperCore.security.service.JwtServerAuthenticationConverter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint

const val ROLE_PREFIX = "ROLE_"
private const val AUTH_PATHS = "/auth/**"
private val SWAGGER_PATHS = listOf(
    "/webjars/swagger-ui/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/openapi/openapi.yaml",
    "/swagger-ui.html"
)

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(
        http: ServerHttpSecurity,
        authenticationWebFilter: AuthenticationWebFilter
    ): SecurityWebFilterChain = http {
        authorizeExchange {
            authorize(AUTH_PATHS, permitAll)
            SWAGGER_PATHS.forEach { authorize(it, permitAll) }
            authorize(anyExchange, authenticated)
        }
        addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        csrf { disable() }
        httpBasic { disable() }
        formLogin { disable() }
        exceptionHandling {
            authenticationEntryPoint = HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
        }
    }

    @Bean
    fun jwtAuthenticationWebFilter(
        authenticationConverter: JwtServerAuthenticationConverter,
        authenticationManager: JwtAuthenticationManager
    ) = AuthenticationWebFilter(authenticationManager).apply {
        setServerAuthenticationConverter(authenticationConverter)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
