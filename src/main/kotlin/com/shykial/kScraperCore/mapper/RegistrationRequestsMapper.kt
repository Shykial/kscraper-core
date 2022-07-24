package com.shykial.kScraperCore.mapper

import com.shykial.kScraperCore.model.entity.ApplicationUser
import generated.com.shykial.kScraperCore.models.RegisterUserRequest

fun RegisterUserRequest.toDocument(passwordHash: String) = ApplicationUser(
    login = login,
    passwordHash = passwordHash,
    email = email
)
