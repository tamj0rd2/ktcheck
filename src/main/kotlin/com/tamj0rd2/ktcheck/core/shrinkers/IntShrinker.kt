package com.tamj0rd2.ktcheck.core.shrinkers

internal object IntShrinker : Shrinker<Int, IntRange> {
    override fun defaultOrigin(range: IntRange): Int = when {
        range.last < 0 -> range.last
        range.first > 0 -> range.first
        else -> 0
    }

    override fun shrink(
        value: Int,
        range: IntRange,
        origin: Int,
    ): Sequence<Int> = sequence {
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
}
