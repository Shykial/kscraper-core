package com.shykial.kScraperCore.helper

import org.assertj.core.api.AbstractIterableAssert

inline fun <SELF : AbstractIterableAssert<SELF, *, *, *>, reified T>
SELF.usingTypeComparator(comparator: Comparator<T>): SELF = usingComparatorForType(comparator, T::class.java)
