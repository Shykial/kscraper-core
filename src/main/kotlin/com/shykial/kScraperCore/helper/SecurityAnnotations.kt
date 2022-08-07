package com.shykial.kScraperCore.helper

import org.springframework.security.access.prepost.PreAuthorize

private const val ADMIN_ROLE_NAME = "ADMIN"
private const val DEV_ROLE_NAME = "DEV"
private const val API_USER_ROLE_NAME = "API_USER"

@PreAuthorize("hasRole('$ADMIN_ROLE_NAME')")
annotation class AllowedForAdmin

@PreAuthorize("hasAnyRole('$ADMIN_ROLE_NAME', '$DEV_ROLE_NAME')")
annotation class AllowedForDev

@PreAuthorize("hasAnyRole('$ADMIN_ROLE_NAME', '$DEV_ROLE_NAME', '$API_USER_ROLE_NAME')")
annotation class AllowedForApiUser
