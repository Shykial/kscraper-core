package com.shykial.kScrapperCore.helpers

import org.bson.internal.Base64

internal fun String.toBase64String() = Base64.encode(toByteArray())

internal fun decodeBase64(string: String) = Base64.decode(string).decodeToString()
