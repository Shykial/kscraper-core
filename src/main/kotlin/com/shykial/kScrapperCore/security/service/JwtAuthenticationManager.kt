package com.shykial.kScrapperCore.security.service

import com.shykial.kScrapperCore.exception.AuthenticationException
import com.shykial.kScrapperCore.repository.ApplicationUserRepository
import com.shykial.kScrapperCore.security.JwtProperties
import com.shykial.kScrapperCore.security.component.DecodedToken
import com.shykial.kScrapperCore.security.component.JwtProvider
import com.shykial.kScrapperCore.security.exception.JwtAuthenticationException
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtServerAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> = mono {
        exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?.substringAfter(JwtProperties.AUTH_HEADER_PREFIX)
            ?.run(::JwtAuthenticationToken)
    }
}

@Component
class JwtAuthenticationManager(
    private val jwtProvider: JwtProvider,
    private val applicationUserRepository: ApplicationUserRepository
) : ReactiveAuthenticationManager {
    private val log = KotlinLogging.logger { }

    override fun authenticate(authentication: Authentication): Mono<Authentication> = mono {
        runCatching {
            (authentication as? JwtAuthenticationToken)
                ?.let { jwtProvider.validateAndDecodeToken(it.token) }
                ?.takeIf { it.refersToValidUser() }
                ?.let {
                    UsernamePasswordAuthenticationToken(it.subject, null, it.roles?.map(::SimpleGrantedAuthority))
                } ?: throw AuthenticationException("Authentication failed for $authentication")
        }.getOrElse {
            log.error(it) { it.message }
            throw JwtAuthenticationException("ex", it)
        }
    }

    private suspend fun DecodedToken.refersToValidUser() =
        subject != null && !roles.isNullOrEmpty() &&
            applicationUserRepository.findByLogin(subject)?.isDisabled == false
}

data class JwtAuthenticationToken(val token: String) : AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {
    override fun getCredentials() = token
    override fun getPrincipal() = token
}
