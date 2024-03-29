package com.shykial.kScraperCore.security.component

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.shykial.kScraperCore.exception.AuthorizationException
import com.shykial.kScraperCore.extension.plusMinutes
import com.shykial.kScraperCore.model.entity.UserRole
import com.shykial.kScraperCore.security.JwtProperties
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties
) {
    private val algorithm = Algorithm.HMAC256(jwtProperties.secret)
    private val verifier = JWT.require(algorithm).build()

    private val basicTokenBuilder: JWTCreator.Builder
        get() = with(jwtProperties) {
            val currentInstant = Instant.now()
            JWT.create()
                .withIssuedAt(currentInstant)
                .withExpiresAt(currentInstant.plusMinutes(validityInMinutes))
                .withJWTId(UUID.randomUUID().toString())
                .withIssuer(issuer)
        }

    fun createToken(
        subject: String,
        roles: Collection<UserRole>,
        additionalClaims: Map<String, Any>? = null
    ): JwtToken = basicTokenBuilder
        .withSubject(subject)
        .withClaim(jwtProperties.rolesClaimName, roles.map(UserRole::name))
        .withPayload(additionalClaims)
        .sign(algorithm)
        .run(::JwtToken)

    fun validateAndDecodeToken(token: String) = runCatching {
        verifier.verify(token)
        DecodedToken(JWT.decode(token), jwtProperties.rolesClaimName)
    }.getOrElse { throw AuthorizationException("Failed to validate provided token", it) }
}

class DecodedToken(
    decodedJWT: DecodedJWT,
    rolesClaimName: String
) {
    val subject: String? = decodedJWT.subject
    val roles: List<String>? = decodedJWT.getClaim(rolesClaimName).asList(String::class.java)
    val issuedAt: Instant? = decodedJWT.issuedAtAsInstant
    val expiresAt: Instant? = decodedJWT.expiresAtAsInstant
    val allClaims: Map<String, Claim> = decodedJWT.claims

    val isExpired: Boolean
        get() = expiresAt?.isBefore(Instant.now()) ?: false
}

@JvmInline
value class JwtToken(val token: String)
