package com.shykial.kScraperCore.helper

import java.time.Instant

fun Instant.plusMinutes(numberOfMinutes: Long): Instant = plusSeconds(numberOfMinutes * 60)
