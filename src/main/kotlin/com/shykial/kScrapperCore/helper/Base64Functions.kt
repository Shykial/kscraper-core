package com.shykial.kScrapperCore.helper

import org.springframework.util.Base64Utils

fun String.toBase64String() = Base64Utils.encodeToString(toByteArray())

fun decodeBase64(encodedString: String) = Base64Utils.decodeFromString(encodedString).decodeToString()
