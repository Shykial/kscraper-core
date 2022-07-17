package com.shykial.kScrapperCore.security.service

import com.shykial.kScrapperCore.repository.ApplicationUserRepository
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ApplicationUserDetailsService(
    private val applicationUserRepository: ApplicationUserRepository
) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> = mono {
        applicationUserRepository.findByLogin(username)?.let { user ->
            User(
                user.login,
                user.passwordHash,
                setOf(SimpleGrantedAuthority(user.role.name))
            )
        }
    }
}
