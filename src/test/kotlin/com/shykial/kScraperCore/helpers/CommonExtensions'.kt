package com.shykial.kScraperCore.helpers

import java.time.Instant

fun Instant.plusMinutes(numberOfMinutes: Long): Instant = plusSeconds(numberOfMinutes * 60)