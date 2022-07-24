package com.shykial.kScrapperCore.helpers

import java.time.Instant

fun Instant.plusMinutes(numberOfMinutes: Long): Instant = plusSeconds(numberOfMinutes * 60)