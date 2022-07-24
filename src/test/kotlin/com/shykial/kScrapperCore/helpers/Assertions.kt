package com.shykial.kScrapperCore.helpers

import io.kotest.matchers.date.shouldBeBetween
import java.time.Duration
import java.time.Instant

fun Instant.shouldBeWithin(margin: Duration, other: Instant) = shouldBeBetween(
    fromInstant = other.minus(margin.dividedBy(2)),
    toInstant = other.plus(margin.dividedBy(2))
)