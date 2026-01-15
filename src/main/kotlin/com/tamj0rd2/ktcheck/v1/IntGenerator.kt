package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.Seed

private data class IntGenerator(
    private val range: IntRange,
) : GenV1<Int>() {
    override fun GenContext.generate(): GenResult<Int> {
        val value = tree.producer.int(range)
        return GenResult(
            value = value,
            shrinks = shrink(value, range).map { tree.withValue(it) }
        )
    }
}

internal fun shrink(value: Int, range: IntRange) = shrink(
    value = value,
    range = range,
    origin = when {
        range.last < 0 -> range.last
        range.first > 0 -> range.first
        else -> 0
    }
)

internal fun shrink(value: Int, range: IntRange, origin: Int): Sequence<Int> = sequence {
    require(origin in range) { "Origin $origin must be within range $range" }

    if (value == origin) return@sequence

    // Always yield the origin first
    yield(origin)

    // Then yield progressively closer values by repeatedly halving the original distance
    val originalDistance = value - origin
    var divisor = 2
    while (true) {
        val shrinkAmount = originalDistance / divisor
        if (shrinkAmount == 0) break

        val candidate = value - shrinkAmount
        if (candidate in range && candidate != origin) {
            yield(candidate)
        }
        divisor *= 2
    }
}


fun GenV1.Companion.int(range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE): GenV1<Int> = IntGenerator(range)

// todo: implement this properly
internal fun GenV1.Companion.long() = GenV1.int().map { it.toLong() }

// todo: move this to a better location
internal fun GenV1.Companion.tree() = GenV1.int().map { ProducerTree.new(Seed(it.toLong())) }
