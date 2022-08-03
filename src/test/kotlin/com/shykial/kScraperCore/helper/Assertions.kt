package com.shykial.kScraperCore.helper

import io.kotest.matchers.date.shouldBeBetween
import org.assertj.core.api.AbstractIterableAssert
import java.time.Duration
import java.time.Instant

fun Instant.shouldBeWithin(margin: Duration, other: Instant) = shouldBeBetween(
    fromInstant = other.minus(margin.dividedBy(2)),
    toInstant = other.plus(margin.dividedBy(2))
)

inline fun <SELF : AbstractIterableAssert<SELF, *, *, *>, reified T>
SELF.usingTypeComparator(comparator: Comparator<T>): SELF = usingComparatorForType(comparator, T::class.java)
