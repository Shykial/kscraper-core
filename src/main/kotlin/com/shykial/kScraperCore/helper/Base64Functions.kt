package com.shykial.kScraperCore.helper

import org.springframework.util.Base64Utils

fun String.toBase64String() = Base64Utils.encodeToString(toByteArray())

fun String.decodeBase64() = Base64Utils.decodeFromString(this).decodeToString()
