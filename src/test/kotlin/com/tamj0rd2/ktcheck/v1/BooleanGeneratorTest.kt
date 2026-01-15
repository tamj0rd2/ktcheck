package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.samples
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class BooleanGeneratorTest : BaseV1GeneratorTest() {
    @Nested
    inner class Generation {
        @Test
        fun `generates a reasonable distribution of values over multiple runs`() {
            withCounter {
                GenV1.bool()
                    .samples()
                    .take(100_000)
                    .forEach { collect(it) }
            }.checkPercentages(mapOf(true to 49.0, false to 49.0))
        }

        @Test
        fun `using the same seed generates the same value`() {
            val gen = GenV1.bool()
            val tree = ProducerTree.new()
            val values = List(1000) { gen.generate(tree) }
            val firstValue = values.first()
            expectThat(values.drop(1)).all { isEqualTo(firstValue) }
        }
    }

    @Nested
    inner class Shrinking {
        // todo: in the future I want to allow the user to specify the shrink direction
        @Test
        fun `true shrinks to false`() {
            val tree = ProducerTree.new().withValue(true)
            val (value, shrinks) = GenV1.bool().generateWithShrunkValues(tree)
            expectThat(value).isTrue()
            expectThat(shrinks).isEqualTo(listOf(false))
        }

        @Test
        fun `false does not shrink`() {
            val tree = ProducerTree.new().withValue(false)
            val (value, shrinks) = GenV1.bool().generateWithShrunkValues(tree)
            expectThat(value).isFalse()
            expectThat(shrinks).isEmpty()
        }
    }
}
