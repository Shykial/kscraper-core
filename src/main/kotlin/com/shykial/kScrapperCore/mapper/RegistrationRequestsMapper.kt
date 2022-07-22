package com.shykial.kScrapperCore.mapper

import com.shykial.kScrapperCore.model.entity.ApplicationUser
import generated.com.shykial.kScrapperCore.models.RegisterUserRequest

fun RegisterUserRequest.toDocument(passwordHash: String) = ApplicationUser(
    login = login,
    passwordHash = passwordHash,
    email = email
)
