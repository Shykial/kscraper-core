package com.shykial.kScraperCore.model.entity

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ApplicationUser(
    @Indexed(unique = true)
    val login: String,

    val passwordHash: String,

    @Indexed(unique = true)
    var email: String,

    val role: UserRole = UserRole.API_USER,

    var enabled: Boolean = true
) : BaseDocument()

enum class UserRole {
    SYSTEM, API_USER, DEV, ADMIN
}
