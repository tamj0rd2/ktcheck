package com.tamj0rd2.ktcheck.core.shrinkers

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isLessThan
import strikt.assertions.isNotEmpty
import kotlin.math.abs

class IntShrinkerTest {
    @Test
    fun `10 shrinks correctly`() {
        val shrinks = IntShrinker.shrink(10, 0..10).toList()
        expectThat(shrinks).isEqualTo(listOf(0, 5, 8, 9))
    }

    @Test
    fun `-10 shrinks correctly`() {
        val shrinks = IntShrinker.shrink(-10, -10..0).toList()
        expectThat(shrinks).isEqualTo(listOf(0, -5, -8, -9))
    }

    @Test
    fun `shrinking produces no shrinks when the original value is the origin`() {
        val range = Int.MIN_VALUE..Int.MAX_VALUE
        generateSequence { range.random() }
            .take(100)
            .forEach { origin ->
                val shrinks = IntShrinker.shrink(origin, range, origin).toList()
                expectThat(shrinks).isEmpty()
            }
    }

    @Test
    fun `shrinks for non-zero numbers always include 0`() {
        val range = -50..50
        generateSequence { range.random() }.take(100).filter { it != 0 }.forEach { value ->
            val shrinks = IntShrinker.shrink(value, range).toList()
            expectThat(shrinks).isNotEmpty().contains(0)
        }
    }

    @Test
    fun `the original generated number is not included in shrinks`() {
        val range = -50..50
        generateSequence { range.random() }.take(100).forEach { value ->
            val shrinks = IntShrinker.shrink(value, range).toList()
            expectThat(shrinks).doesNotContain(value)
        }
    }

    @Test
    fun `when 0 is in range, shrinks are closer to 0 than the original generated number`() {
        val range = -50..50
        generateSequence { range.random() }.take(100).filter { it != 0 }.forEach { value ->
            val shrinks = IntShrinker.shrink(value, range).toList()
            expectThat(shrinks)
                .isNotEmpty()
                .doesNotContain(value)
                .all {
                    get { abs(this) }.describedAs("shrunk distance from 0").isLessThan(abs(value))
                }
        }
    }

    @Test
    fun `shrinks with custom origin in positive range`() {
        val shrinks = IntShrinker.shrink(10, 0..10, 5).toList()
        expectThat(shrinks).isEqualTo(listOf(5, 8, 9))
    }

    @Test
    fun `shrinks with custom origin in negative range`() {
        val shrinks = IntShrinker.shrink(-10, -20..-10, -15).toList()
        expectThat(shrinks).isEqualTo(listOf(-15, -12, -11))
    }

    @Test
    fun `shrinks with custom origin in mixed range`() {
        val shrinks = IntShrinker.shrink(7, -5..10, 3).toList()
        expectThat(shrinks).isEqualTo(listOf(3, 5, 6))
    }

    @Test
    fun `shrink throws if origin not in range`() {
        expectThrows<IllegalArgumentException> {
            IntShrinker.shrink(10, 0..10, 20).toList()
        }
    }
}
