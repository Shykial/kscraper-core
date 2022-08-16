package com.shykial.kScraperCore.extension

import java.time.Instant

fun Instant.plusMinutes(numberOfMinutes: Long): Instant = plusSeconds(numberOfMinutes * 60)
