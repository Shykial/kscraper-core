package com.shykial.kScrapperCore.model.entity

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

    var isDisabled: Boolean = false,

    var isEnabled: Boolean = false
) : BaseDocument()

enum class UserRole {
    API_USER, DEV, ADMIN
}
