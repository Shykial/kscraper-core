package com.shykial.kScrapperCore.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "security.jwt")
class JwtProperties(
    val secret: String,
    val validityInMinutes: Long,
    val issuer: String,
    val rolesClaimName: String
) {
    companion object {
        const val AUTH_HEADER_PREFIX = "Bearer "
    }
}
